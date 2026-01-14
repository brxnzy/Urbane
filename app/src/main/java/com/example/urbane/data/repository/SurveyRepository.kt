package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Survey
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
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

}