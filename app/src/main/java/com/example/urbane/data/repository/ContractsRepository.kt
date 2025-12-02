package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Contract
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
class ContractsRepository(val sessionManager: SessionManager) {
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

    suspend fun updateContractConditions(contractId: Int, conditions: String): Boolean {
        return try {
            supabase.from("contracts").update({
                set("conditions", conditions)
            }) {
                filter {
                    eq("id", contractId)
                }
            }
            true
        } catch (e: Exception) {
            throw Exception("Error actualizando condiciones: ${e.message}")
        }
    }
}