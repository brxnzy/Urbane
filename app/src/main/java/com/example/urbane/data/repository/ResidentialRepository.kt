package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residential
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

class ResidentialRepository(val sessionManager: SessionManager) {

    suspend fun getResidentialId(name: String): Int {
        return try{
        val result = supabase.from("residentials")
            .select(columns = Columns.list("id")){
                filter {
                    eq("residentialName",name)
                }
            }.decodeSingle<Int>()

         result
        } catch (e: Exception){
            Log.e("Error obteniendo el id del residencial", e.toString())

        }


    }


        // ✅ Reutilizar la función pero desde la sesión actual
        suspend fun getUserResidentials(): List<Residential> {
            return try {
                val currentUser = sessionManager.sessionFlow.first()
                val userId = currentUser?.userId ?: return emptyList()

                val urrList = supabase.from("users_residentials_roles")
                    .select(columns = Columns.list("residential_id")) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<UrrResidentialId>()

                urrList.map { urr ->
                    supabase.from("residentials")
                        .select(columns = Columns.list("id", "name", "address", "phone", "logoUrl")) {
                            filter {
                                eq("id", urr.residential_id)
                            }
                        }
                        .decodeSingle<Residential>()
                }
            } catch (e: Exception) {
                Log.e("ResidentialsManagementRepo", "Error: ${e.message}")
                emptyList()
            }
        }

        suspend fun updateResidential(residential: Residential): Boolean {
            return try {
                supabase.from("residentials")
                    .update({
                        set("name", residential.name)
                        set("address", residential.address)
                        set("phone", residential.phone)
                        set("logoUrl", residential.logoUrl)
                    }) {
                        filter {
                            eq("id", residential.id)
                        }
                    }
                true
            } catch (e: Exception) {
                Log.e("ResidentialRepository", "Error actualizando: ${e.message}")
                false
            }
        }

        // ✅ Crear nuevo residencial y asignarlo al usuario actual
        suspend fun createResidential(
            name: String,
            address: String?,
            phone: String?,
            logoUrl: String?
        ): Boolean {
            return try {
                val currentUser = sessionManager.sessionFlow.first()
                val userId = currentUser?.userId ?: return false
                val roleId = currentUser.roleId.toIntOrNull() ?: 1

                // Crear residencial
                val newResidential = supabase.from("residentials")
                    .insert(mapOf(
                        "name" to name,
                        "address" to address,
                        "phone" to phone,
                        "logoUrl" to logoUrl
                    )) {
                        select()
                    }
                    .decodeSingle<Residential>()

                // Asignar al usuario
                supabase.from("users_residentials_roles")
                    .insert(mapOf(
                        "user_id" to userId,
                        "residential_id" to newResidential.id,
                        "role_id" to roleId
                    ))

                true
            } catch (e: Exception) {
                Log.e("ResidentialsManagementRepo", "Error creando: ${e.message}")
                false
            }
        }

        // ✅ Eliminar asignación de residencial (no elimina el residencial, solo la relación)
        suspend fun removeResidentialAssignment(residentialId: Int): Boolean {
            return try {
                val currentUser = sessionManager.sessionFlow.first()
                val userId = currentUser?.userId ?: return false

                supabase.from("users_residentials_roles")
                    .delete {
                        filter {
                            eq("user_id", userId)
                            eq("residential_id", residentialId)
                        }
                    }
                true
            } catch (e: Exception) {
                Log.e("ResidentialsManagementRepo", "Error eliminando asignación: ${e.message}")
                false
            }
        }

        @Serializable
        data class UrrResidentialId(val residential_id: Int)
    }
