package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val idCard: String,
    val password: String,
    val role_id: Int,
    val residence_id: Int? = null,
    val residential_id: Int?
)