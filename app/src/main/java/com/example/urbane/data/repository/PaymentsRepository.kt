package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.PaymentTransaction
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns


class PaymentRepository (
    private val sessionManager: SessionManager
) {


    private fun getResidentialId(sessionManager: SessionManager): Int? {
        return try {
            getResidentialId(sessionManager)

        } catch (e: Exception) {
            Log.e("PaymentRepository", "Error obteniendo residential_id: $e")
            null
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

        payments.forEach { p ->
            val transactionData = PaymentTransaction(paymentId = p.paymentId, amount = p.montoPagar, method = "Efectivo")
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


}

