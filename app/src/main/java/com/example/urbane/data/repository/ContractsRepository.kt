package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from


class ContractsRepository(val sessionManager: SessionManager) {

    suspend fun getContracts(){
        try {
            val residentialId = getResidentialId(sessionManager) ?: emptyList<String>()
            supabase.from("contracts").select(){
                filter { eq("residentialId",residentialId) }
            }
        }catch (e: Exception){
            Log.d("ContractsRepository", "Error al obtener los contratos: ${e.message}")
        }
    }
}