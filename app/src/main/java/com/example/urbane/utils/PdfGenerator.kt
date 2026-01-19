package com.example.urbane.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.urbane.data.model.Transaction
import com.example.urbane.ui.admin.finances.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f

        @RequiresApi(Build.VERSION_CODES.Q)
        suspend fun generateFinancialReport(
            context: Context,
            startDate: String,
            endDate: String,
            transactions: List<Transaction>,
            totalIngresos: Double,
            totalEgresos: Double
        ): Pair<Uri?, String?> = withContext(Dispatchers.IO) {
            try {
                // Crear documento PDF
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                var yPosition = MARGIN

                // Paint para título
                val titlePaint = Paint().apply {
                    textSize = 24f
                    isFakeBoldText = true
                    color = android.graphics.Color.BLACK
                }

                // Paint para subtítulos
                val subtitlePaint = Paint().apply {
                    textSize = 18f
                    isFakeBoldText = true
                    color = android.graphics.Color.BLACK
                }

                // Paint para texto normal
                val textPaint = Paint().apply {
                    textSize = 12f
                    color = android.graphics.Color.BLACK
                }

                // Paint para montos
                val amountPaint = Paint().apply {
                    textSize = 14f
                    isFakeBoldText = true
                }

                // Título del reporte
                canvas.drawText("Reporte Financiero", MARGIN, yPosition, titlePaint)
                yPosition += LINE_HEIGHT * 2

                // Fechas
                canvas.drawText("Período: $startDate - $endDate", MARGIN, yPosition, textPaint)
                yPosition += LINE_HEIGHT * 2

                // Resumen financiero
                canvas.drawText("Resumen Financiero", MARGIN, yPosition, subtitlePaint)
                yPosition += LINE_HEIGHT * 1.5f

                // Total Ingresos
                amountPaint.color = android.graphics.Color.rgb(76, 175, 80) // Verde
                canvas.drawText("Total Ingresos:", MARGIN, yPosition, textPaint)
                canvas.drawText("$${"%.2f".format(totalIngresos)}",
                    PAGE_WIDTH - MARGIN - 100, yPosition, amountPaint)
                yPosition += LINE_HEIGHT

                // Total Egresos
                amountPaint.color = android.graphics.Color.rgb(244, 67, 54) // Rojo
                canvas.drawText("Total Egresos:", MARGIN, yPosition, textPaint)
                canvas.drawText("$${"%.2f".format(totalEgresos)}",
                    PAGE_WIDTH - MARGIN - 100, yPosition, amountPaint)
                yPosition += LINE_HEIGHT

                // Balance
                val balance = totalIngresos - totalEgresos
                amountPaint.color = if (balance >= 0)
                    android.graphics.Color.rgb(76, 175, 80)
                else
                    android.graphics.Color.rgb(244, 67, 54)
                canvas.drawText("Balance:", MARGIN, yPosition, textPaint)
                canvas.drawText("$${"%.2f".format(balance)}",
                    PAGE_WIDTH - MARGIN - 100, yPosition, amountPaint)
                yPosition += LINE_HEIGHT * 2

                // Línea divisoria
                canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, textPaint)
                yPosition += LINE_HEIGHT

                // Detalle de transacciones - Ingresos
                val ingresos = transactions.filter { it.type == TransactionType.INGRESO }
                if (ingresos.isNotEmpty()) {
                    canvas.drawText("Ingresos (${ingresos.size})", MARGIN, yPosition, subtitlePaint)
                    yPosition += LINE_HEIGHT * 1.5f

                    ingresos.forEach { transaction ->
                        if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                            pdfDocument.finishPage(page)
                            val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                            val newPage = pdfDocument.startPage(newPageInfo)
                            canvas.save()
                            yPosition = MARGIN
                        }

                        canvas.drawText(formatDate(transaction.date) ?: "-", MARGIN, yPosition, textPaint)
                        canvas.drawText(transaction.description ?: "Sin descripción",
                            MARGIN + 100, yPosition, textPaint)
                        canvas.drawText("$${"%.2f".format(transaction.amount)}",
                            PAGE_WIDTH - MARGIN - 100, yPosition, textPaint)
                        yPosition += LINE_HEIGHT
                    }
                    yPosition += LINE_HEIGHT
                }

                // Detalle de transacciones - Egresos
                val egresos = transactions.filter { it.type == TransactionType.EGRESO }
                if (egresos.isNotEmpty()) {
                    if (yPosition > PAGE_HEIGHT - MARGIN * 4) {
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                        val newPage = pdfDocument.startPage(newPageInfo)
                        canvas.save()
                        yPosition = MARGIN
                    }

                    canvas.drawText("Egresos (${egresos.size})", MARGIN, yPosition, subtitlePaint)
                    yPosition += LINE_HEIGHT * 1.5f

                    egresos.forEach { transaction ->
                        if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                            pdfDocument.finishPage(page)
                            val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                            val newPage = pdfDocument.startPage(newPageInfo)
                            canvas.save()
                            yPosition = MARGIN
                        }

                        canvas.drawText(formatDate(transaction.date) ?: "-", MARGIN, yPosition, textPaint)
                        canvas.drawText(transaction.description ?: "Sin descripción",
                            MARGIN + 100, yPosition, textPaint)
                        canvas.drawText("$${"%.2f".format(transaction.amount)}",
                            PAGE_WIDTH - MARGIN - 100, yPosition, textPaint)
                        yPosition += LINE_HEIGHT
                    }
                }

                // Finalizar página
                pdfDocument.finishPage(page)

                // ✅ GUARDAR EN DESCARGAS usando MediaStore
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "Reporte_Financiero_$timestamp.pdf"

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

                var outputStream: OutputStream? = null
                uri?.let {
                    outputStream = resolver.openOutputStream(it)
                    outputStream?.use { out ->
                        pdfDocument.writeTo(out)
                    }

                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }

                pdfDocument.close()

                Log.d("PdfGenerator", "PDF generado exitosamente en Descargas: $fileName")
                Pair(uri, fileName)

            } catch (e: Exception) {
                Log.e("PdfGenerator", "Error generando PDF: ${e.message}", e)
                Pair(null, null)
            }
        }

        private fun formatDate(dateString: String?): String? {
            return try {
                if (dateString == null) return null
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: return null)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}