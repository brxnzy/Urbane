package com.example.urbane.data.model
import kotlinx.serialization.Serializable

@Serializable
data class IncidentCategory(
    val id: Int,
    val name: String,
//    val residentialId: Int
)
