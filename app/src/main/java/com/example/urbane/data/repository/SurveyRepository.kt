package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Survey
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getCurrentUserId
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class SurveyRepository(val sessionManager: SessionManager) {

    suspend fun getAllSurveys(): List<Survey> {
        try {
            val residentialId = getResidentialId(sessionManager)

            val response = supabase.from("surveys")
                .select(
                    columns = Columns.raw(
                        """
                    id,
                    residentialId,
                    question,
                    active,
                    createdAt,
                    closedAt,
                    options:surveys_option(
                        id,
                        surveyId,
                        text,
                        votes:survey_votes(
                            id,
                            surveyId,
                            optionId,
                            residentId,
                            resident:users(
                                id,
                                name,
                                photoUrl
                            )
                        )
                    )
                    """.trimIndent()
                    )
                ) {
                    filter {
                        eq("residentialId", residentialId!!)
                    }
                    order("createdAt", Order.DESCENDING)
                }
                .decodeList<Survey>()

            Log.d("SurveyRepository", "Encuestas obtenidas: ${response.size}")
            response.forEach { survey ->
                Log.d(
                    "SurveyRepository",
                    "Survey: ${survey.question}, Options: ${survey.options.size}"
                )
                survey.options.forEach { option ->
                    Log.d("SurveyRepository", "  - ${option.text}: ${option.votes.size} votos")
                }
            }
            return response

        } catch (e: Exception) {
            Log.e("SurveyRepository", "Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun createSurvey(question: String, options: List<String>) {
        try {
            val residentialId = getResidentialId(sessionManager)
                ?: throw Exception("No hay residential ID")

            val surveyResponse = supabase.from("surveys")
                .insert(
                    buildJsonObject {
                        put("question", question)
                        put("residentialId", residentialId)
                        put("active", true)
                    }
                ) {
                    select(Columns.raw("id"))
                }
                .decodeSingle<JsonObject>()

            val surveyId: JsonElement? = surveyResponse["id"]

            val optionsToInsert = options.map { optionText ->
                buildJsonObject {
                    put("surveyId", surveyId!!)
                    put("text", optionText)
                }
            }

            supabase.from("surveys_option")
                .insert(optionsToInsert)

            Log.d("SurveyRepository", "Survey created successfully with ID: $surveyId")

        } catch (e: Exception) {
            Log.e("SurveyRepository", "Error creating survey: ${e.message}", e)
            throw Exception("Error al crear la encuesta: ${e.message}")
        }
    }

    suspend fun showAvailableSurvey(): List<Survey> {
        try {
            val residentialId = getResidentialId(sessionManager)
                ?: throw Exception("No hay residential ID")

            val userId = getCurrentUserId(sessionManager)

            val votedSurveyIds = supabase.from("survey_votes")
                .select(columns = Columns.list("surveyId")) {
                    filter { eq("residentId", userId) }
                }
                .decodeList<JsonObject>()
                .mapNotNull { it["surveyId"]?.jsonPrimitive?.intOrNull }
                .toSet()

            Log.d("SurveyRepository", "User $userId has voted in surveys: $votedSurveyIds")

            val allSurveys = supabase.from("surveys").select(
                columns = Columns.raw(
                    """
                id,
                residentialId,
                question,
                active,
                createdAt,
                closedAt,
                options:surveys_option(
                    id,
                    surveyId,
                    text
                )
                """.trimIndent()
                )
            ) {
                filter { eq("residentialId", residentialId) }
                filter { eq("active", true) }
            }.decodeList<Survey>()

            // Paso 3: Filtrar las encuestas donde NO ha votado
            val availableSurveys = allSurveys.filter { survey ->
                survey.id !in votedSurveyIds
            }

            Log.d("SurveyRepository", "Total: ${allSurveys.size}, Available: ${availableSurveys.size}")

            return availableSurveys

        } catch (e: Exception) {
            Log.e("SurveyRepository", "error fetching available surveys: ${e.message}", e)
            throw e
        }
    }

    suspend fun voteSurvey(surveyId: Int, optionId: Int) {
        try {
            val userId = getCurrentUserId(sessionManager)

            supabase.from("survey_votes")
                .insert(
                    buildJsonObject {
                        put("surveyId", surveyId)
                        put("optionId", optionId)
                        put("residentId", userId)
                    }
                )

            Log.d("SurveyRepository", "Vote registered: Survey $surveyId, Option $optionId")

        } catch (e: Exception) {
            Log.e("SurveyRepository", "Error voting: ${e.message}", e)
            throw Exception("Error al registrar el voto: ${e.message}")
        }
    }
}