package com.example.urbane.data.repository


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.InvoiceData
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.PaymentTransaction
import com.example.urbane.data.model.Service
import com.example.urbane.data.model.TransactionDetail
import com.example.urbane.data.model.User
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.Instant


class PaymentRepository(
    private val sessionManager: SessionManager
) {
    val auditLogRepository = AuditLogsRepository(sessionManager)
    suspend fun getPaymentsByUser(id: String): List<Payment> {
        return try {
            supabase
                .from("payments")
                .select(
                    columns = Columns.list(
                        "id",
                        "residentId",
                        "month",
                        "year",
                        "amount",
                        "paidAmount",
                        "status",
                        "createdAt",
                        "paymentTransactions:payments_transactions(*)",
                        "fines:fines(*)"
                    )
                ) {
                    filter { eq("residentId", id) }
                    order("year", Order.ASCENDING)
                    order("month", Order.ASCENDING)
                }
                .decodeList<Payment>()

        } catch (e: Exception) {

            Log.e("PaymentRepository", "Error obteniendo pagos: $e")
            throw e
        }
    }


    suspend fun registerPayment(payments: List<SelectedPayment>): List<Int> {
        val residentialId = getResidentialId(sessionManager) ?: 0
        val transactionIds = mutableListOf<Int>()
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val shortId = java.util.UUID.randomUUID().toString().substring(0, 8)
        val invoiceFileName = "factura_${dateStr}_$shortId.pdf"

        payments.forEach { p ->

            Log.d(
                "PAYMENTBUG",
                """
    registerPayments → INICIO
    paymentId=${p.paymentId}
    mes=${p.mes}/${p.year}
    montoTotalBase=${p.montoTotal}
    montoPendienteCuota=${p.montoPendiente}
    totalMultas=${p.totalMultas}
    montoPagarVisual=${p.montoPagar}
    isPagoCompleto=${p.isPagoCompleto}
    invoiceFileName=$invoiceFileName
    """.trimIndent()
            )

            // 1️⃣ Insertar transacción CON el fileName
            val transactionData = PaymentTransaction(
                paymentId = p.paymentId,
                amount = p.montoPagar,
                method = "Efectivo",
                residentialId = residentialId,
                invoiceFileName = invoiceFileName
            )

            val inserted = supabase.from("payments_transactions")
                .insert(transactionData) { select() }
                .decodeSingle<JsonObject>()

            transactionIds.add(inserted["id"]!!.jsonPrimitive.int)

            Log.d(
                "PAYMENTBUG",
                "registerPayments → transacción insertada id=${inserted["id"]!!.jsonPrimitive.int} monto=${p.montoPagar}"
            )

            // 2️⃣ Traer payment REAL desde BD
            val paymentDb = supabase.from("payments")
                .select { filter { eq("id", p.paymentId) } }
                .decodeSingle<Payment>()

            Log.d(
                "PAYMENTBUG",
                """
    registerPayments → payment DB
    amount=${paymentDb.amount}
    paidAmount=${paymentDb.paidAmount}
    """.trimIndent()
            )

            val resident = supabase.from("users")
                .select(columns = Columns.list("id","name")) {
                    filter { eq("id", paymentDb.residentId) }
                }
                .decodeSingle<User>()

            val nuevoPaidAmount = paymentDb.paidAmount + p.montoPagar
            val deudaTotal = paymentDb.amount + p.totalMultas
            val pendienteTotal = (deudaTotal - nuevoPaidAmount).coerceAtLeast(0f)

            // 5️⃣ ✅ Status basado en la deuda TOTAL (cuota + multas)
            val nuevoStatus = when {
                pendienteTotal <= 0f -> "Pagado"
                nuevoPaidAmount > 0f -> "Parcial"
                else -> "Pendiente"
            }

            Log.d(
                "PAYMENTBUG",
                """
    registerPayments → CALCULO
    montoPagar=${p.montoPagar}
    cuotaBase=${paymentDb.amount}
    totalMultas=${p.totalMultas}
    deudaTotal=$deudaTotal
    nuevoPaidAmount=$nuevoPaidAmount
    pendienteTotal=$pendienteTotal
    nuevoStatus=$nuevoStatus
    """.trimIndent()
            )

            // 6️⃣ Update del payment
            supabase.from("payments")
                .update({
                    set("paidAmount", nuevoPaidAmount)
                    set("status", nuevoStatus)
                }) {
                    filter { eq("id", p.paymentId) }
                }

            Log.d("PAYMENTBUG", "registerPayments → UPDATE realizado paymentId=${p.paymentId}")

            // Log de auditoría
            auditLogRepository.logAction(
                action = "PAYMENT_REGISTERED",
                entity = "payments",
                entityId = p.paymentId.toString(),
                data = buildJsonObject {
                    put("residentName", JsonPrimitive(resident.name))
                    put("month", JsonPrimitive(p.mes))
                    put("year", JsonPrimitive(p.year))
                    put("amountPaid", JsonPrimitive(p.montoPagar))
                    put("previousPaidAmount", JsonPrimitive(paymentDb.paidAmount))
                    put("newPaidAmount", JsonPrimitive(nuevoPaidAmount))
                    put("previousStatus", JsonPrimitive(paymentDb.status))
                    put("newStatus", JsonPrimitive(nuevoStatus))
                    put("invoiceFileName", JsonPrimitive(invoiceFileName))
                }
            )
        }

        return transactionIds
    }

    suspend fun getAllPayments(): List<Payment> {
        return try {
            val residentialId = getResidentialId(sessionManager)
                ?: throw IllegalStateException("No residentialId en sesión")

            val pagosBase = supabase.from("payments")
                .select(
                    Columns.raw(
                        """
                    id,
                    residentId,
                    month,
                    year,
                    amount,
                    paidAmount,
                    status,
                    createdAt,
                    resident:users(*)
                    """.trimIndent()
                    )
                ) {
                    filter { eq("residentialId", residentialId) }
                    order("year", Order.DESCENDING)
                    order("month", Order.DESCENDING)
                }
                .decodeList<Payment>()

            pagosBase.map { pago ->
                val transacciones = getPaymentTransactions(pago.id!!)
                pago.copy(paymentTransactions = transacciones)
            }

        } catch (e: Exception) {
            Log.e("PaymentRepository", "Error obteniendo pagos: $e")
            throw IllegalStateException("Error obteniendo pagos: $e")
        }
    }


    suspend fun getPaymentTransactions(paymentId: Int): List<PaymentTransaction> {
        return try {
            val residentialId = getResidentialId(sessionManager) ?: 0

            supabase.from("payments_transactions").select {
                filter {
                    eq("residentialId", residentialId)
                    eq("paymentId", paymentId)
                }
            }
                .decodeList<PaymentTransaction>()
        } catch (e: Exception) {
            throw IllegalStateException("Error obteniendo transacciones de pago: $e")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun getTransactionDetailById(transactionId: Int): TransactionDetail {
        try {
            val residentialId = getResidentialId(sessionManager)
                ?: throw IllegalStateException("No hay residentialId")

            val data = supabase.from("payments_transactions").select(
                Columns.raw(
                    """
                id,
                amount,
                invoiceFileName,
                payment:payments(
                    id,
                    month,
                    year,
                    amount,
                    paidAmount,
                    status,
                    resident:users(id, name)
                )
                """
                )
            ) {
                filter {
                    eq("residentialId", residentialId)
                    eq("id", transactionId)
                }
            }.decodeSingle<JsonObject>()

            val payment = data["payment"]?.jsonObject
                ?: throw IllegalStateException("Payment no encontrado")

            val resident = payment["resident"]?.jsonObject
                ?: throw IllegalStateException("Resident no encontrado")

            val paymentId = payment["id"]!!.jsonPrimitive.int
            val residentId = resident["id"]!!.jsonPrimitive.content

            val contractData = supabase.from("contracts").select(
                Columns.raw(
                    """
                id,
                amount
                """
                )
            ) {
                filter {
                    eq("residentId", residentId)
                    eq("active", true)
                }
            }.decodeSingleOrNull<JsonObject>() // ✅ EVITA CRASH

            val contractId = contractData?.get("id")?.jsonPrimitive?.int
            val contractAmount = contractData?.get("amount")?.jsonPrimitive?.float

            // 3. Servicios del contrato (si hay contrato)
            val services = if (contractId != null) {
                val servicesData = supabase.from("contract_services").select(
                    Columns.raw(
                        """
                    service:services(id, name, price)
                    """
                    )
                ) {
                    filter {
                        eq("contractId", contractId)
                    }
                }.decodeList<JsonObject>()

                servicesData.mapNotNull { item ->
                    val service = item["service"]?.jsonObject ?: return@mapNotNull null
                    Service(
                        id = service["id"]!!.jsonPrimitive.int,
                        name = service["name"]!!.jsonPrimitive.content,
                        price = service["price"]!!.jsonPrimitive.float
                    )
                }
            } else {
                emptyList()
            }

            val invoiceFileName = data["invoiceFileName"]?.jsonPrimitive?.content

            // 4. Resultado final
            return TransactionDetail(
                transactionId = data["id"]?.jsonPrimitive?.int,
                transactionAmount = data["amount"]?.jsonPrimitive?.float,
                paymentId = paymentId,
                residentId = residentId,
                month = payment["month"]?.jsonPrimitive?.int,
                year = payment["year"]?.jsonPrimitive?.int,
                paymentStatus = payment["status"]?.jsonPrimitive?.content,
                paymentAmount = payment["amount"]?.jsonPrimitive?.float,
                paidAmount = payment["paidAmount"]?.jsonPrimitive?.float,
                residentName = resident["name"]?.jsonPrimitive?.content,
                contractAmount = contractAmount,
                services = services,
                invoiceFileName = invoiceFileName
            )


        } catch (e: Exception) {
            throw IllegalStateException("Error obteniendo detalle de transacción: $e")
        }
    }

    suspend fun buildInvoiceFromTransactions(
        transactionIds: List<Int>
    ): InvoiceData {

        val lines = mutableListOf<TransactionDetail>()

        transactionIds.forEach { id ->
            val detail = getTransactionDetailById(id)
            lines.add(detail)
        }

        val totalAmount = lines.sumOf { (it.paymentAmount ?: 0f).toDouble() }.toFloat()
        val totalPaid = lines.sumOf { (it.transactionAmount ?: 0f).toDouble() }.toFloat()
        val totalRemaining = lines.sumOf {
            val total = it.paymentAmount ?: 0f
            val paid = it.paidAmount ?: 0f
            (total - paid).toDouble()
        }.toFloat()

        Log.d("PaymentRepository", "buildInvoiceFromTransactions: $lines")

        val first = lines.firstOrNull()
        val invoiceFileName = first?.invoiceFileName

        return InvoiceData(
            invoiceId = System.currentTimeMillis().toString(),
            createdAt = Instant.now().toString(),
            residentId = first?.residentId,
            residentName = first?.residentName,
            lines = lines,
            totalAmount = totalAmount,
            totalPaid = totalPaid,
            totalRemaining = totalRemaining,
            invoiceFileName = invoiceFileName
        )
    }


    suspend fun generateAndUploadInvoice(
        context: Context,
        invoice: InvoiceData
    ): String {
        val fileName = invoice.invoiceFileName ?: "factura_${invoice.invoiceId}.pdf"


        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val colorPrimary = Color.parseColor("#157F3D")
        val colorSecondary = Color.parseColor("#1E40AF") // Azul oscuro
        val colorSuccess = Color.parseColor("#059669") // Verde
        val colorWarning = Color.parseColor("#D97706") // Naranja
        val colorText = Color.parseColor("#1F2937") // Gris oscuro
        val colorTextLight = Color.parseColor("#6B7280") // Gris claro
        val colorBorder = Color.parseColor("#E5E7EB") // Gris muy claro

        // Pinceles
        val paintTitle = Paint().apply {
            textSize = 24f
            color = colorPrimary
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintHeader = Paint().apply {
            textSize = 14f
            color = colorText
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintNormal = Paint().apply {
            textSize = 11f
            color = colorText
            isAntiAlias = true
        }

        val paintSmall = Paint().apply {
            textSize = 9f
            color = colorTextLight
            isAntiAlias = true
        }

        val paintTableHeader = Paint().apply {
            textSize = 10f
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTableCell = Paint().apply {
            textSize = 10f
            color = colorText
            isAntiAlias = true
        }

        val paintTotal = Paint().apply {
            textSize = 16f
            color = colorSuccess
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // ====================
        // HEADER CON FONDO
        // ====================
        var y = 0f

        // Rectángulo superior azul
        val paintHeaderBg = Paint().apply {
            color = colorPrimary
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 120f, paintHeaderBg)

        // Título principal
        y = 50f
        paintTitle.color = Color.WHITE
        canvas.drawText("FACTURA DE PAGO", 40f, y, paintTitle)

        // Info de factura en header (blanco)
        y = 75f
        paintHeader.color = Color.WHITE
        paintHeader.textSize = 11f
        canvas.drawText("Factura #${invoice.invoiceId}", 40f, y, paintHeader)

        // Fecha alineada a la derecha
        val dateText = "Fecha: ${formatDate(invoice.createdAt)}"
        val dateWidth = paintHeader.measureText(dateText)
        canvas.drawText(dateText, 555f - dateWidth, y, paintHeader)

        // ====================
        // INFORMACIÓN DEL RESIDENTE
        // ====================
        y = 160f
        paintHeader.color = colorPrimary
        paintHeader.textSize = 12f
        canvas.drawText("INFORMACIÓN DEL RESIDENTE", 40f, y, paintHeader)

        // Línea decorativa
        val paintLine = Paint().apply {
            color = colorPrimary
            strokeWidth = 2f
        }
        y += 5f
        canvas.drawLine(40f, y, 200f, y, paintLine)

        y += 20f
        paintNormal.color = colorText
        canvas.drawText("Nombre:", 40f, y, paintNormal)
        paintNormal.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(invoice.residentName ?: "N/A", 120f, y, paintNormal)
        paintNormal.typeface = Typeface.DEFAULT

        y += 18f
        canvas.drawText("ID Residente:", 40f, y, paintNormal)
        paintNormal.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("#${invoice.residentId}", 120f, y, paintNormal)
        paintNormal.typeface = Typeface.DEFAULT

        // ====================
        // TABLA DE DETALLES
        // ====================
        y += 40f
        paintHeader.color = colorPrimary
        paintHeader.textSize = 12f
        canvas.drawText("DETALLE DE PAGOS", 40f, y, paintHeader)

        y += 5f
        canvas.drawLine(40f, y, 555f, y, paintLine)

        y += 20f

        // Header de tabla con fondo
        val tableTop = y
        val paintTableBg = Paint().apply {
            color = colorSecondary
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, y - 15f, 555f, y + 10f, paintTableBg)

        // Columnas: Mes/Año | Concepto | Monto | Pagado | Estado
        val col1 = 50f
        val col2 = 140f
        val col3 = 280f
        val col4 = 370f
        val col5 = 460f

        canvas.drawText("MES/AÑO", col1, y, paintTableHeader)
        canvas.drawText("CONCEPTO", col2, y, paintTableHeader)
        canvas.drawText("MONTO", col3, y, paintTableHeader)
        canvas.drawText("PAGADO", col4, y, paintTableHeader)
        canvas.drawText("ESTADO", col5, y, paintTableHeader)

        y += 25f

        // Pincel para bordes de celdas
        val paintCellBorder = Paint().apply {
            color = colorBorder
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        // Renderizar filas
        invoice.lines.forEachIndexed { index, line ->
            val rowTop = y - 15f
            val rowBottom = y + 10f

            // Fondo alternado
            if (index % 2 == 0) {
                val paintRowBg = Paint().apply {
                    color = Color.parseColor("#F9FAFB")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(40f, rowTop, 555f, rowBottom, paintRowBg)
            }

            // Datos
            val monthYear = "${line.month?.toString()?.padStart(2, '0')}/${line.year}"
            canvas.drawText(monthYear, col1, y, paintTableCell)

            canvas.drawText("Cuota Mensual", col2, y, paintTableCell)

            val amount = String.format("$%.2f", line.paymentAmount ?: 0f)
            canvas.drawText(amount, col3, y, paintTableCell)

            val paid = String.format("$%.2f", line.transactionAmount ?: 0f)
            canvas.drawText(paid, col4, y, paintTableCell)

            // Estado con color
            val status = line.paymentStatus ?: "Pendiente"
            val paintStatus = Paint(paintTableCell).apply {
                color = when (status) {
                    "Pagado" -> colorSuccess
                    "Parcial" -> colorWarning
                    else -> Color.parseColor("#DC2626")
                }
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(status, col5, y, paintStatus)

            // Borde inferior de celda
            canvas.drawLine(40f, rowBottom, 555f, rowBottom, paintCellBorder)

            y += 25f
        }

        // Borde de tabla
        canvas.drawRect(40f, tableTop - 15f, 555f, y - 15f, paintCellBorder)

        // ====================
        // RESUMEN FINANCIERO
        // ====================
        y += 20f

        // Caja de totales
        val boxTop = y
        val paintBoxBg = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(340f, y, 555f, y + 100f, paintBoxBg)

        val paintBoxBorder = Paint().apply {
            color = colorPrimary
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(340f, y, 555f, y + 100f, paintBoxBorder)

        y += 25f
        paintNormal.textSize = 11f
        canvas.drawText("Monto Total:", 355f, y, paintNormal)
        val totalAmount = String.format("$%.2f", invoice.totalAmount)
        val totalAmountWidth = paintNormal.measureText(totalAmount)
        canvas.drawText(totalAmount, 540f - totalAmountWidth, y, paintNormal)

        y += 20f
        canvas.drawText("Total Pagado:", 355f, y, paintNormal)
        paintTotal.textSize = 12f
        paintTotal.color = colorSuccess
        val totalPaid = String.format("$%.2f", invoice.totalPaid)
        val totalPaidWidth = paintTotal.measureText(totalPaid)
        canvas.drawText(totalPaid, 540f - totalPaidWidth, y, paintTotal)

        y += 20f
        canvas.drawText("Saldo Pendiente:", 355f, y, paintNormal)
        val remainingColor = if (invoice.totalRemaining > 0) colorWarning else colorSuccess
        paintTotal.color = remainingColor
        val totalRemaining = String.format("$%.2f", invoice.totalRemaining)
        val totalRemainingWidth = paintTotal.measureText(totalRemaining)
        canvas.drawText(totalRemaining, 540f - totalRemainingWidth, y, paintTotal)

        // ====================
        // FOOTER
        // ====================
        y = 800f
        paintSmall.textAlign = Paint.Align.CENTER
        paintSmall.color = colorTextLight
        canvas.drawText("Gracias por su pago puntual", 297.5f, y, paintSmall)

        y += 15f
        canvas.drawText("Este documento es un comprobante oficial de pago", 297.5f, y, paintSmall)

        // Línea decorativa footer
        val paintFooterLine = Paint().apply {
            color = colorBorder
            strokeWidth = 1f
        }
        canvas.drawLine(100f, y + 10f, 495f, y + 10f, paintFooterLine)

        pdfDocument.finishPage(page)

        // Guardar archivo
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }
        pdfDocument.close()

        // Subir a Supabase
        val bytes = file.readBytes()
        supabase.storage.from("invoices").upload(
            path = fileName,
            data = bytes,
        )

        return supabase.storage
            .from("invoices")
            .publicUrl(fileName)
    }

    // Función auxiliar para formatear fecha
    private fun formatDate(isoDate: String?): String {
        if (isoDate == null) return "N/A"
        return try {
            val instant = Instant.parse(isoDate)
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            formatter.format(java.util.Date.from(instant))
        } catch (e: Exception) {
            isoDate.substring(0, 10)
        }
    }

    suspend fun updateInvoiceUrlForTransactions(
        transactionIds: List<Int>,
        invoiceUrl: String
    ) {
        supabase.from("payments_transactions")
            .update({
                set("invoiceUrl", invoiceUrl)
            }) {
                filter {
                    isIn("id", transactionIds)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun downloadInvoiceFromSupabase(
        context: Context,
        fileUrl: String,
        fileName: String
    ): Uri? = withContext(Dispatchers.IO) {

        try {
            // ✅ DESCARGA EN HILO DE FONDO
            val connection = URL(fileUrl).openConnection()
            connection.connect()
            val bytes = connection.getInputStream().readBytes()

            // ✅ GUARDAR EN DESCARGAS
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )

            uri?.let {
                resolver.openOutputStream(it)?.use { out ->
                    out.write(bytes)
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            uri
        } catch (e: Exception) {
            Log.e("DownloadInvoice", "Error descargando factura", e)
            null
        }
    }


}