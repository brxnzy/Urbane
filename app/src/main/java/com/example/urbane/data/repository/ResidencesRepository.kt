package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residence
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ResidencesRepository(val sessionManager: SessionManager) {

    val auditLogRepository = AuditLogsRepository(sessionManager)

    suspend fun createResidence(
        name: String,
        type: String,
        description: String
    ) {
        try {
            val residentialId = getResidentialId(sessionManager)

            val residence = Residence(
                name = name,
                type = type,
                description = description,
                available = true,
                residentialId = residentialId
            )

            val result = supabase
                .from("residences")
                .insert(residence) {
                    select()
                }.decodeSingle<Residence>()

            print(result)

            Log.d("ResidencesRepository", "intentando loguear la accion")
            auditLogRepository.logAction(
                action = "RESIDENCE_CREATED",
                entity = "residences",
                entityId = result.id.toString(),
                data = buildJsonObject {
                    put("name", JsonPrimitive(name))
                    put("type", JsonPrimitive(type))
                }
            )

        } catch (e: Exception) {
            Log.e("ResidencesRepository", e.toString())
        }
    }

    suspend fun getResidences(): List<Residence> {
        try {
            val residentialId = getResidentialId(sessionManager) ?: emptyList<Residence>()

            val residences = supabase
                .from("residences")
                .select() {
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
                .select() {
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

            // Obtener datos de la residencia antes de vaciar
            val residence = getResidenceById(id)

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

            // Log de auditoría
            auditLogRepository.logAction(
                action = "RESIDENCE_VACATED",
                entity = "residences",
                entityId = id.toString(),
                data = buildJsonObject {
                    put("residenceName", JsonPrimitive(residence.name))
                    put("residentId", JsonPrimitive(residentId))
                    put("vacateDate", JsonPrimitive(today))
                }
            )

            return getResidenceById(id)

        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en vacateResidence: $e")
            throw e
        }
    }

    suspend fun updateResidence(id: Int, name: String, type: String, description: String) {
        try {
            // Obtener datos ANTES del update
            val oldResidence = getResidenceById(id)

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

            // Log de auditoría con valores viejos y nuevos
            auditLogRepository.logAction(
                action = "RESIDENCE_UPDATED",
                entity = "residences",
                entityId = id.toString(),
                data = buildJsonObject {
                    put("oldName", JsonPrimitive(oldResidence.name))
                    put("newName", JsonPrimitive(name))
                    put("oldType", JsonPrimitive(oldResidence.type))
                    put("newType", JsonPrimitive(type))
                    put("oldDescription", JsonPrimitive(oldResidence.description ?: ""))
                    put("newDescription", JsonPrimitive(description))
                }
            )

            Log.d("ResidencesRepository", "Residencia actualizada exitosamente")
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en updateResidence: $e")
            throw e
        }
    }

    suspend fun deleteResidence(id: Int) {
        try {
            // Obtener datos antes de eliminar
            val residence = getResidenceById(id)

            supabase.from("residences")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }

            // Log de auditoría
            auditLogRepository.logAction(
                action = "RESIDENCE_DELETED",
                entity = "residences",
                entityId = id.toString(),
                data = buildJsonObject {
                    put("name", JsonPrimitive(residence.name))
                    put("type", JsonPrimitive(residence.type))
                }
            )

            Log.d("ResidencesRepository", "Residencia eliminada exitosamente")
        } catch (e: Exception) {
            Log.e("ResidencesRepository", "Error en deleteResidence: $e")
            throw e
        }
    }
}