package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SurveyVotes(
    val id: Int,
    val surveyId: Int,
    val optionId: Int,
    val resident: UserMinimal
)