package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Contract(
    val id: Int? = null,
    val residentId: String,
    val residentialId: Int,
    val residenceId: Int,
    val startDate: String,
    val endDate: String? = null,
    val conditions: String? = null,
    val residentName: String? = null,
    val residentPhotoUrl: String? = null,
    val residenceName: String? = null,
    val residenceType: String? = null
)
