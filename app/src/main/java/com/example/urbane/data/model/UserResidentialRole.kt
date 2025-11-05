package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResidentialRole(
    val user: User,
    val residential: Residential,
    val role: Role
)
