package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Contract(
    val id: Int? = null,
    val residenceId: Int,
    val residentId: String,
    val startDate: String,
    val endDate: String? = null,
    val monthlyAmount: Float? = null,
    val conditions: String? = null,
    val residentialId: Int
)