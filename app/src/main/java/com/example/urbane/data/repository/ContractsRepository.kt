package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Contract
import com.example.urbane.data.model.Residence
import com.example.urbane.data.model.User
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class   ContractsRepository(val sessionManager: SessionManager) {
    val auditLogRepository = AuditLogsRepository(sessionManager)
    suspend fun getContracts(): List<Contract>{
        try {
            val residentialId = getResidentialId(sessionManager) ?: emptyList<String>()
            val contracts = supabase.from("contracts_view").select{
                filter { eq("residentialId",residentialId) }
            }.decodeList<Contract>()
            return contracts
        }catch (e: Exception){
            Log.d("ContractsRepository", "Error al obtener los contratos: ${e.message}")
            throw e
        }
    }

    suspend fun getContractById(id: Int): Contract{
        try {
            val residentialId = getResidentialId(sessionManager) ?: emptyList<String>()
            val contract = supabase.from("contracts_view").select{
                filter {
                    eq("residentialId",residentialId)
                    eq("id",id)
                }
            }.decodeSingle<Contract>()
            return contract
        }catch (e: Exception){
            Log.d("ContractsRepository", "Error al obtener el contrato por id: ${e.message}")
            throw e
        }
    }

    suspend fun updateContract(contractId: Int, conditions: String, amount: Double): Boolean {
        return try {
            // Obtener datos del contrato ANTES de actualizar
            val oldContract = supabase.from("contracts")
                .select(columns = Columns.list(
                    "id",
                    "conditions",
                    "amount",
                    "residentId",
                    "residenceId"
                )) {
                    filter {
                        eq("id", contractId)
                    }
                }
                .decodeSingle<Contract>()

            // Obtener nombre del residente
            val resident = supabase.from("users")
                .select(columns = Columns.list("name")) {
                    filter {
                        eq("id", oldContract.residentId)
                    }
                }
                .decodeSingle<User>()

            val residence = supabase.from("residences")
                .select(columns = Columns.list("name")) {
                    filter {
                        eq("id", oldContract.residenceId)
                    }
                }
                .decodeSingle<Residence>()

            // Actualizar el contrato
            supabase.from("contracts").update({
                set("conditions", conditions)
                set("amount", amount)
            }) {
                filter {
                    eq("id", contractId)
                }
            }

            // Log de auditoría
            auditLogRepository.logAction(
                action = "CONTRACT_UPDATED",
                entity = "contracts",
                entityId = contractId.toString(),
                data = buildJsonObject {
                    put("residentName", JsonPrimitive(resident.name))
                    put("residenceName", JsonPrimitive(residence.name))
                    put("oldConditions", JsonPrimitive(oldContract.conditions ?: ""))
                    put("newConditions", JsonPrimitive(conditions))
                    put("oldAmount", JsonPrimitive(oldContract.amount ?: 0.0))
                    put("newAmount", JsonPrimitive(amount))
                }
            )

            true
        } catch (e: Exception) {
            throw Exception("Error actualizando contrato: ${e.message}")
        }
    }
}