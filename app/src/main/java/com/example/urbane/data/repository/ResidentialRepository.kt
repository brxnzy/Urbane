package com.example.urbane.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residential
import com.example.urbane.data.model.UrrIds
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

class ResidentialRepository(val sessionManager: SessionManager,private val context: Context) {
    private val bucketName = "residential_logos"

    suspend fun getResidentialId(name: String): Int {
        return try {
            val result = supabase.from("residentials")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("residentialName", name)
                    }
                }.decodeSingle<Int>()

            result
        } catch (e: Exception) {
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
            Log.e("ResidentialsRepo", "Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateResidential(
        residential: Residential,
        newImageUri: Uri?
    ): Boolean {
        return try {
            var finalLogoUrl = residential.logoUrl

            if (newImageUri != null) {
                val fileName = residential.name.replace(" ", "_")

                val inputStream = context.contentResolver.openInputStream(newImageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    supabase.storage.from(bucketName).upload(
                        path = fileName,
                        data = bytes,
                    )

                    // Obtener URL pública
                    finalLogoUrl = supabase.storage.from(bucketName).publicUrl(fileName)
                }
            }

            supabase.from("residentials")
                .update({
                    set("name", residential.name)
                    set("address", residential.address)
                    set("phone", residential.phone)
                    set("logoUrl", finalLogoUrl)
                }) {
                    filter {
                        eq("id", residential.id!!)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e("ResidentialsRepo", "Error actualizando: ${e.message}")
            false
        }
    }

    suspend fun createResidential(
        name: String,
        address: String,
        phone: String,
        imageUri: Uri?
    ): Boolean {
        return try {
            val currentUser = sessionManager.sessionFlow.first()
            val userId = currentUser?.userId ?: return false
            val roleId = currentUser.roleId.toIntOrNull() ?: 1

            val newResidential = supabase.from("residentials")
                .insert(
                    Residential(
                        name =  name,
                        address = address,
                        phone =  phone,
                        logoUrl = null
                    )
                ) {
                    select()
                }
                .decodeSingle<Residential>()

            if (imageUri != null) {
                val fileName = name.replace(" ", "_")

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    supabase.storage.from(bucketName).upload(
                        path = fileName,
                        data = bytes,
                    )

                    val logoUrl = supabase.storage.from(bucketName).publicUrl(fileName)

                    supabase.from("residentials")
                        .update({
                            set("logoUrl", logoUrl)
                        }) {
                            filter {
                                eq("id", newResidential.id!!)
                            }
                        }
                }
            }

            supabase.from("users_residentials_roles")
                .insert(
                    UrrIds(
                         userId,
                         newResidential.id!!,
                        roleId
                    )
                )

            true
        } catch (e: Exception) {
            Log.e("ResidentialsRepo", "Error creando: ${e.message}")
            false
        }
    }

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
            Log.e("ResidentialsRepo", "Error eliminando: ${e.message}")
            false
        }
    }

    @Serializable
    data class UrrResidentialId(val residential_id: Int)
}
