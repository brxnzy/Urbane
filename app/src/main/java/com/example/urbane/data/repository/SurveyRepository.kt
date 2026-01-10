package com.example.urbane.data.repository
import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Survey
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

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
}