package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Fine(
    val id: Int?,
    val residentId: String,
    val paymentId: Int?,
    val title: String,
    val description: String?,
    val amount: Float,
    val status: String,
    val resident: UserMinimal? = null,
    val residentialId: Int?,
    val createdAt: String
)