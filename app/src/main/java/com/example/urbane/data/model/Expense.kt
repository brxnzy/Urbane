package com.example.urbane.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: Int? = null,
    val amount: Double,
    val description: String,
    val adminId: String,
    val admin: UserMinimal? = null,
    val createdAt: String? = null,
    val residentialId: Int,
)
