package com.example.urbane.ui.resident.model
import com.example.urbane.data.model.Survey

data class ResidentHomeState(
    val isLoadingSurveys: Boolean = false,
    val availableSurveys: List<Survey> = emptyList(),
    val surveysError: String? = null,
    val selectedOptions: Map<Int, Int> = emptyMap()
)



sealed class ResidentHomeIntent {
    data class SelectSurveyOption(val surveyId: Int, val optionId: Int) : ResidentHomeIntent()
    data class VoteSurvey(val surveyId: Int) : ResidentHomeIntent()
}