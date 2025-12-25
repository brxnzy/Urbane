package com.example.urbane.data.repository


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
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
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.Instant


class PaymentRepository(
    private val sessionManager: SessionManager
) {



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

            Log.e("PaymentRepository","Error obteniendo pagos: $e")
            throw e
        }
    }

    suspend fun registerPayment(payments: List<SelectedPayment>): List<Int> {
        val residentialId = getResidentialId(sessionManager) ?: 0
        val transactionIds = mutableListOf<Int>()

        // ✅ Generar nombre de factura ÚNICO para todas las transacciones
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
                invoiceFileName = invoiceFileName  // ✅ NUEVO
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

            // 3️⃣ ✅ Actualizar solo el paidAmount (dinero recibido)
            val nuevoPaidAmount = paymentDb.paidAmount + p.montoPagar

            // 4️⃣ ✅ Calcular pendiente real: (cuota base + multas) - total pagado
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
                    order("year", Order.ASCENDING)
                    order("month", Order.ASCENDING)
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

        // ✅ Usar el fileName que ya viene en invoice
        val fileName = invoice.invoiceFileName ?: "factura_${invoice.invoiceId}.pdf"

        // 1. Generar PDF local
        val pdfDocument = PdfDocument()
        val paint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 40
        paint.textSize = 16f
        canvas.drawText("FACTURA DE PAGO", 200f, y.toFloat(), paint)

        y += 25
        paint.textSize = 12f
        canvas.drawText("Factura: ${invoice.invoiceId}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Fecha: ${invoice.createdAt}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Residente: ${invoice.residentName}", 40f, y.toFloat(), paint)

        y += 30
        canvas.drawText("DETALLE:", 40f, y.toFloat(), paint)
        y += 20

        invoice.lines.forEach {
            val line = "Mes ${it.month}/${it.year} - Pagado: ${it.transactionAmount} - Estado: ${it.paymentStatus}"
            canvas.drawText(line, 40f, y.toFloat(), paint)
            y += 18
        }

        y += 25
        canvas.drawText("TOTAL PAGADO: ${invoice.totalPaid}", 40f, y.toFloat(), paint)
        y += 20
        canvas.drawText("TOTAL RESTANTE: ${invoice.totalRemaining}", 40f, y.toFloat(), paint)

        pdfDocument.finishPage(page)

        // ✅ Guardar con el nombre correcto
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use {
            pdfDocument.writeTo(it)
        }

        pdfDocument.close()

        val bytes = file.readBytes()

        // ✅ Subir con el mismo nombre
        supabase.storage.from("invoices").upload(
            path = fileName,
            data = bytes,
        )

        return supabase.storage
            .from("invoices")
            .publicUrl(fileName)
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










