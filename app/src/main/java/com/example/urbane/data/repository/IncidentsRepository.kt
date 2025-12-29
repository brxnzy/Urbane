package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Incident
import com.example.urbane.data.model.IncidentCategory
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

class IncidentsRepository(val sessionManager: SessionManager) {

    suspend fun getIncidentCategories(): List<IncidentCategory> {
        return try {
            val residentialId = getResidentialId(sessionManager)
            supabase.from("incidents_categories").select {
                filter {
                    eq("residentialId", residentialId!!)
                }
            }.decodeList<IncidentCategory>()

        } catch (e: Exception) {
            Log.e("IncidentsRepository", "error obteniendo las categorias de incidencias $e")
            throw e
        }
    }

    suspend fun getIncidents(): List<Incident> {
        return try {
            val residentialId = getResidentialId(sessionManager)

            val incidents = supabase.from("incidents").select(columns = Columns.list(
                "id",
                "createdAt",
                "title",
                "description",
                "status",
                "type",
                "residentId",
                "residentName:users(name)",
                "residentialId",
                "scheduledDate",
                "startTime",
            )) {
                filter {
                    eq("residentialId", residentialId!!)
                }
            }.decodeList<Incident>()


            val incidentIds = incidents.mapNotNull { it.id }

            if (incidentIds.isEmpty()) {
                return emptyList()
            }

            val imagesJson = supabase.from("incident_images").select {
                filter {
                    isIn("incidentId", incidentIds)
                }
            }.decodeList<JsonObject>()

            // 4. Agrupar URLs por incidentId
            val imageUrlsByIncident = imagesJson
                .groupBy { it["incidentId"]?.jsonPrimitive?.int }
                .mapValues { entry ->
                    entry.value.mapNotNull {
                        it["imageUrl"]?.jsonPrimitive?.contentOrNull
                    }
                }

            incidents.map { incident ->
                incident.copy(
                    imageUrls = imageUrlsByIncident[incident.id] ?: emptyList()
                )
            }

        } catch (e: Exception) {
            Log.e("IncidentsRepository", "Error obteniendo las incidencias: $e")
            throw e
        }
    }

    // En tu IncidentsRepository.kt

    suspend fun attendIncident(
        incidentId: Int,
        scheduledDate: String,
        startTime: String,
        adminResponse: String
    ) {
        try {
            supabase.from("incidents")
                .update({
                    set("status", "Atendido")
                    set("scheduledDate", scheduledDate)
                    set("startTime", startTime)
                    set("adminResponse", adminResponse)
                }) {
                    filter {
                        eq("id", incidentId)
                    }
                }
        } catch (e: Exception) {
            Log.e("IncidentsRepository", "Error atendiendo incidencia: $e")
            throw e
        }
    }
}