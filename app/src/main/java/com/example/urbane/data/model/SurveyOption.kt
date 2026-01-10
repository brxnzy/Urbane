package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SurveyOption(
    val id: Int,
    val surveyId: Int,
    val text: String,
    val votes: List<SurveyVotes> = emptyList()
)
