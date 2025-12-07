package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Payment
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.payments.model.*
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order


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

}

