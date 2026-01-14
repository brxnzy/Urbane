package com.example.urbane.ui.admin.settings.model
import com.example.urbane.data.model.Survey
data class SurveysState(
    val isLoading: Boolean = false,
    val surveys: List<Survey> = emptyList(),
    val errorMessage: String? = null,
    val showBottomSheet: Boolean = false,
    val question: String = "",
    val options: List<String> = listOf("", ""),
    val canSave: Boolean = false
)
sealed class SurveysIntent {
    data object ShowCreateSheet : SurveysIntent()
    data object DismissBottomSheet : SurveysIntent()
    data class UpdateQuestion(val question: String) : SurveysIntent()
    data class UpdateOption(val index: Int, val value: String) : SurveysIntent()
    data object AddOption : SurveysIntent()
    data class RemoveOption(val index: Int) : SurveysIntent()
    data object CreateSurvey : SurveysIntent()
}