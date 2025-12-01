package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager

import com.example.urbane.data.model.Residence
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.firstOrNull

class ResidencesRepository(val sessionManager: SessionManager) {
    suspend fun createResidence(name:String, type:String, description:String){
        try {
            val residentialId = getResidentialId(sessionManager)
            Log.d("ResidencesRepository","intentando crear residencia")
            val data = Residence(name =name, type =type, description =description, available = true, residentialId =residentialId)
            Log.d("ResidencesRepository","DATOS A INSERTAR $data")
            supabase.from("residences").insert(data)
        }catch (e: Exception){

            Log.e("ResidencesRepository",e.toString())
        }

    }


    suspend fun getResidences(): List<Residence> {
        try {
            val residentialId = getResidentialId(sessionManager) ?: emptyList<Residence>()

            val residences = supabase
                .from("residences")
                .select (){
                    filter {
                        eq("residentialId", residentialId)
                    }
                }
                .decodeList<Residence>()

            return residences
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en getResidences: $e")
            throw e
        }
    }

    suspend fun getResidenceById(id: Int): Residence {
        return try {
            supabase
                .from("residences_view")
                .select (){
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Residence>()
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en getResidences: $e")
            throw e
        }
    }

    suspend fun vacateResidence(id: Int, residentId: String): Residence {
        try {
            val today = java.time.LocalDate.now().toString()

            supabase.postgrest.rpc(
                "reset_user_residential_role",
                mapOf("uid" to residentId)
            )

            supabase.from("users")
                .update(
                    {
                        set("active", false)
                    }
                ) {
                    filter {
                        eq("id", residentId)
                    }
                }

            supabase.from("contracts")
                .update(
                    {
                        set("active", false)
                        set("endDate", today)
                    }
                ) {
                    filter {
                        eq("residentId", residentId)
                        eq("active", true)
                    }
                }

            return getResidenceById(id)

        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en vacateResidence: $e")
            throw e
        }
    }

    suspend fun updateResidence(id: Int, name: String, type: String, description: String) {
        try {
            supabase.from("residences")
                .update(
                    {
                        set("name", name)
                        set("type", type)
                        set("description", description)
                    }
                ) {
                    filter {
                        eq("id", id)
                    }
                }
            Log.d("ResidencesRepository", "Residencia actualizada exitosamente")
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en updateResidence: $e")
            throw e
        }
    }

    suspend fun deleteResidence(id: Int) {
        try {
            supabase.from("residences")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Log.d("ResidencesRepository", "Residencia eliminada exitosamente")
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en deleteResidence: $e")
            throw e
        }
    }


}