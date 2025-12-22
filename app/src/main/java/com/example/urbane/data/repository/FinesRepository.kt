package com.example.urbane.data.repository                                                                                                                                                                                                          
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Fine
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
                .select(columns = Columns.list(
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
                ) ){
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

    suspend fun createFine(
        residentId: String,
        title: String,
        description: String?,
        amount: String
    ) {
        val residentialId = getResidentialId(sessionManager)
            ?: error("No residentialId")

        supabase.from("fines").insert(mapOf(
            "residentId" to residentId,
            "title" to title,
            "description" to description,
            "amount" to amount,
            "status" to "Pendiente",
            "residentialId" to residentialId
        )
            )
    }

}
