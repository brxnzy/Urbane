package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Survey (
    val id: Int,
    val question: String,
    val active: Boolean,
    val createdAt: String,
    val closedAt: String? = null,
    val residentialId: Int,
    val options: List<SurveyOption> = emptyList()
)



