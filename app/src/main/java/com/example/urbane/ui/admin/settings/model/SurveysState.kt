package com.example.urbane.ui.admin.settings.model
import com.example.urbane.data.model.Survey

data class SurveysState(
    val isLoading : Boolean = false,
    val surveys : List<Survey> = emptyList(),
    val errorMessage : String? = null,

)