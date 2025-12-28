package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Incident
import com.example.urbane.data.model.IncidentCategory
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
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

            // 1. Obtener incidencias
            val incidents = supabase.from("incidents").select {
                filter {
                    eq("residentialId", residentialId!!)
                }
            }.decodeList<Incident>()

            // 2. Obtener IDs
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
}