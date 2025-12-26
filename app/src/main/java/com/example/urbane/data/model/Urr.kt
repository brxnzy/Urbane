package com.example.urbane.data.model
import kotlinx.serialization.Serializable
@Serializable
data class UrrIds(
    val user_id: String,
    val residential_id: Int,
    val role_id: Int
)
