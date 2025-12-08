package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.PaymentTransaction
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order


class PaymentRepository (
    private val sessionManager: SessionManager
) {


    private suspend fun getResId(sessionManager: SessionManager): Int? {
        return try {
            getResidentialId(sessionManager)

        } catch (e: Exception) {
            Log.e("PaymentRepository", "Error obteniendo residential_id: $e")

        }
    }

    suspend fun getPaymentsByUser(id: String): List<Payment> {
        return try {

            supabase
                .from("payments")
                .select(columns = Columns.list("id",
                    "residentId",
                    "month",
                    "year",
                    "amount",
                    "paidAmount",
                    "status",
                    "createdAt",
                    "paymentsTransactions:payments_transactions(*)")){

                    filter {
                        eq("residentId", id)
                    }
                }
                .decodeList<Payment>()

        } catch (e: Exception) {
            throw IllegalStateException("Error obteniendo pagos: $e")
        }
    }

    suspend fun registerPayment(payments: List<SelectedPayment>) {
        val residentialId = getResidentialId(sessionManager) ?:0


        payments.forEach { p ->
            val transactionData = PaymentTransaction(paymentId = p.paymentId, amount = p.montoPagar, method = "Efectivo", residentialId = residentialId)
            supabase.from("payments_transactions")
                .insert(transactionData)

            val paidAmount = p.montoTotal - p.montoPendiente   // lo ya pagado
            val nuevoPaidAmount = paidAmount + p.montoPagar    // sumas lo que acaba de pagar

            val nuevoPendiente = p.montoTotal - nuevoPaidAmount

            val nuevoStatus = when {
                nuevoPendiente <= 0f -> "Pagado"
                nuevoPaidAmount > 0f -> "Parcial"
                else -> "Pendiente"
            }

            val updateData = mapOf(
                "paidAmount" to nuevoPaidAmount,
                "status" to nuevoStatus
            )

            supabase.from("payments")
                .update(
                    {
                    set("paidAmount", nuevoPaidAmount)
                    set("status", nuevoStatus)
                    }
                ) {
                    filter {
                        eq("id", p.paymentId)
                    }
                }
        }
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

            supabase.from("payments_transactions").select{
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


}

