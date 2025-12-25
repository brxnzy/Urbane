package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Fine
import com.example.urbane.data.model.PaymentPeriod
import com.example.urbane.data.model.UserMinimal
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class FinesRepository(
    private val sessionManager: SessionManager
) {

    suspend fun getAllFines(): List<Fine> {
        return try {
            val residentialId = getResidentialId(sessionManager)
                ?: error("No residentialId")

            val fines = supabase
                .from("fines")
                .select(
                    columns = Columns.list(
                        """
                    id,
                    createdAt,
                    residentId,
                    paymentId,
                    title,
                    description,
                    amount,
                    status,
                    residentialId
                    """
                    )
                ) {
                    filter { eq("residentialId", residentialId) }
                }
                .decodeList<Fine>()

            val users = supabase
                .from("users")
                .select(columns = Columns.list("id, name, photoUrl"))
                .decodeList<UserMinimal>()
                .associateBy { it.id }

            fines.map { fine ->
                fine.copy(
                    resident = users[fine.residentId]
                )
            }

        } catch (e: Exception) {
            Log.e("FinesRepository", "Error obteniendo multas", e)
            emptyList()
        }

    }


    suspend fun getFineById(fineId: Int): Fine {
        return try {
            val residentialId = getResidentialId(sessionManager)
                ?: error("No residentialId")

            val fine = supabase
                .from("fines")
                .select(
                    columns = Columns.list(
                        """
                    id,
                    createdAt,
                    residentId,
                    paymentId,
                    title,
                    description,
                    amount,
                    status,
                    residentialId
                    """
                    )
                ) {
                    filter {
                        eq("id", fineId)
                        eq("residentialId", residentialId)
                    }
                }
                .decodeSingle<Fine>()

            val resident = supabase
                .from("users")
                .select(columns = Columns.list("id, name, photoUrl")) {
                    filter {
                        eq("id", fine.residentId)

                    }
                }
                .decodeSingle<UserMinimal>()

            val paymentPeriod = fine.paymentId?.let { paymentId ->
                supabase
                    .from("payments")
                    .select(columns = Columns.list("month, year")) {
                        filter {
                            eq("id", paymentId)
                        }
                    }

                    .decodeSingle<PaymentPeriod>()
            }


            fine.copy(
                resident = resident,
                paymentPeriod = paymentPeriod
            )

        } catch (e: Exception) {
            Log.e("FinesRepository", "Error obteniendo multa por id", e)
            throw e
        }
    }

    suspend fun createFine(
        residentId: String,
        title: String,
        description: String?,
        amount: String
    ) {
        try {
            val residentialId = getResidentialId(sessionManager)
                ?: error("No residentialId")

            val data = Fine(
                residentId = residentId,
                title = title,
                description = description,
                amount = amount.toFloat(),
                status = "Pendiente",
                residentialId = residentialId,
            )

            supabase.from("fines").insert(data)

        } catch (e: Exception) {
            Log.e("FinesRepository", "Error creando multa", e)
            throw e
        }
    }

    suspend fun cancelFine(fineId: Int) {
        try {
            supabase.from("fines").update({
                set("status", "Cancelada")
            }) {
                filter {
                    eq("id", fineId)
                }
            }
        } catch (e: Exception) {
            Log.e("FinesRepository", "Error cancelando multa $e")
        }
    }


}
