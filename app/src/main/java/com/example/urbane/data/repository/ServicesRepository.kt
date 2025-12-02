package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.ContractService
import com.example.urbane.data.model.Service
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from


class ServicesRepository(val sessionManager: SessionManager) {

    suspend fun getContractServices(id: Int): List<ContractService> {
        return try {
            supabase.from("contract_services_view").select{
                filter {
                    eq("contractId", id)
                }
            }.decodeList<ContractService>()

        } catch (e: Exception) {
            throw Exception("Error obteniendo servicios del contrato: ${e.message}")
        }
    }

    suspend fun getAllServices(): List<Service> {
         try {
            val residentialId = getResidentialId(sessionManager) ?: throw Exception("Error obteniendo residentialId")
            val services = supabase
                .from("services").select{
                    filter {
                        eq("residentialId", residentialId)
                    }
                }
                .decodeList<Service>()

            Log.d("ServicesRepository", "Services: $services")
            return services
        } catch (e: Exception) {
            throw Exception("Error obteniendo servicios: ${e.message}")
        }
    }

    suspend fun addServiceToContract(contractId: Int, serviceId: Int): Boolean {
        return try {
            supabase.from("contract_services").insert(
                mapOf(
                    "contractId" to contractId,
                    "serviceId" to serviceId
                )
            )
            true
        } catch (e: Exception) {
            throw Exception("Error agregando servicio al contrato: ${e.message}")
        }
    }

    suspend fun removeServiceFromContract(contractServiceId: Int): Boolean {
        return try {
            supabase.from("contract_services").delete {
                filter {
                    eq("id", contractServiceId)
                }
            }
            true
        } catch (e: Exception) {
            throw Exception("Error eliminando servicio del contrato: ${e.message}")
        }
    }
}

// MODELOS




