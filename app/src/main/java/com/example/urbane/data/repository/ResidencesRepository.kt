package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager

import com.example.urbane.data.model.Residence
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.firstOrNull

class ResidencesRepository(val sessionManager: SessionManager) {

    suspend fun getResidentialId(): Int? {
        val user = sessionManager.sessionFlow.firstOrNull()
        return user?.userData?.residential?.id
    }

    suspend fun createResidence(name:String, type:String, description:String, residentialId:Int){
        try {
            Log.d("ResidencesRepository","intentando crear residencia")
            val data = Residence(name=name, type=type, description=description, residentialId=residentialId)
            supabase.from("residences").insert(data)
        }catch (e: Exception){

            Log.e("ResidencesRepository",e.toString())
        }

    }


    suspend fun getResidences(): List<Residence> {
        return try {
            val residentialId = getResidentialId() ?: emptyList<Residence>()

            supabase
                .from("residences")
                .select (){
                    filter {
                        eq("residentialId", residentialId)
                    }
                }
                .decodeList<Residence>()
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en getResidences: $e")
            throw e
        }
    }

    suspend fun getAvailableResidences(residentialId: Int): List<Residence> {
        return try {
            val residentialId = getResidentialId() ?: emptyList<Residence>()

            supabase
                .from("residences")
                .select(
                    columns = Columns.list()
                ) {
                    filter {
                        eq("residentialId", residentialId)
                        eq("available", true)
                    }
                }
                .decodeList<Residence>()
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en getResidences: $e")
            throw e
        }
    }
}