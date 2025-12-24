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

        Log.d("PaymentsRepo", "⏳ INICIO registerPayment")
        Log.d("PaymentsRepo", "📌 Cantidad de pagos recibidos: ${payments.size}")

        val residentialId = getResidentialId(sessionManager) ?: 0
        Log.d("PaymentsRepo", "🏢 residentialId: $residentialId")

        val transactionIds = mutableListOf<Int>()

        payments.forEachIndexed { index, p ->

            Log.d("PaymentsRepo", "➡️ Procesando pago #$index")
            Log.d("PaymentsRepo", "   paymentId: ${p.paymentId}")
            Log.d("PaymentsRepo", "   montoPagar: ${p.montoPagar}")
            Log.d("PaymentsRepo", "   montoTotal: ${p.montoTotal}")
            Log.d("PaymentsRepo", "   montoPendiente: ${p.montoPendiente}")

            val transactionData = PaymentTransaction(
                paymentId = p.paymentId,
                amount = p.montoPagar,
                method = "Efectivo",
                residentialId = residentialId
            )

            Log.d("PaymentsRepo", "🚀 Insertando en payments_transactions: $transactionData")

            val inserted = supabase.from("payments_transactions")
                .insert(transactionData) {
                    select()
                }
                .decodeSingle<JsonObject>()

            val transactionId = inserted["id"]!!.jsonPrimitive.int
            transactionIds.add(transactionId)

            Log.d("PaymentsRepo", "✅ transactionId generado: $transactionId")



            Log.d("PaymentsRepo", "✅ transactionId generado: $transactionId")

            val paidAmount = p.montoTotal - p.montoPendiente
            val nuevoPaidAmount = paidAmount + p.montoPagar
            val nuevoPendiente = p.montoTotal - nuevoPaidAmount

            val nuevoStatus = when {
                nuevoPendiente <= 0f -> "Pagado"
                nuevoPaidAmount > 0f -> "Parcial"
                else -> "Pendiente"
            }

            Log.d("PaymentsRepo", "🧮 paidAmount viejo: $paidAmount")
            Log.d("PaymentsRepo", "🧮 nuevoPaidAmount: $nuevoPaidAmount")
            Log.d("PaymentsRepo", "🧮 nuevoPendiente: $nuevoPendiente")
            Log.d("PaymentsRepo", "🧾 nuevoStatus: $nuevoStatus")

            Log.d("PaymentsRepo", "🔄 Actualizando payment id ${p.paymentId}")

            supabase.from("payments")
                .update({
                    set("paidAmount", nuevoPaidAmount)
                    set("status", nuevoStatus)
                }) {
                    filter { eq("id", p.paymentId) }
                }

            Log.d("PaymentsRepo", "✅ Payment ${p.paymentId} actualizado correctamente")
        }

        Log.d("PaymentsRepo", "🏁 FIN registerPayment")
        Log.d("PaymentsRepo", "📦 Transaction IDs finales: $transactionIds")

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

            // Agregar transacciones por pago
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
                services = services
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

        return InvoiceData(
            invoiceId = System.currentTimeMillis().toString(),
            createdAt = Instant.now().toString(),
            residentId = first?.residentId,
            residentName = first?.residentName,
            lines = lines,
            totalAmount = totalAmount,
            totalPaid = totalPaid,
            totalRemaining = totalRemaining
        )
    }


        suspend fun generateAndUploadInvoice(
            context: Context,
            invoice: InvoiceData
        ): String {

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
                val line =
                    "Mes ${it.month}/${it.year} - Pagado: ${it.transactionAmount} - Estado: ${it.paymentStatus}"
                canvas.drawText(line, 40f, y.toFloat(), paint)
                y += 18
            }

            y += 25
            canvas.drawText("TOTAL PAGADO: ${invoice.totalPaid}", 40f, y.toFloat(), paint)
            y += 20
            canvas.drawText("TOTAL RESTANTE: ${invoice.totalRemaining}", 40f, y.toFloat(), paint)

            pdfDocument.finishPage(page)

            val file = File(
                context.cacheDir,
                "factura_${invoice.invoiceId}.pdf"
            )

            FileOutputStream(file).use {
                pdfDocument.writeTo(it)
            }

            pdfDocument.close()

            val bytes = file.readBytes()
            val path = "factura_${invoice.invoiceId}.pdf"

            supabase.storage.from("invoices").upload(
                path = path,
                data = bytes,
            )

            return supabase.storage
                .from("invoices")
                .publicUrl(path)
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










