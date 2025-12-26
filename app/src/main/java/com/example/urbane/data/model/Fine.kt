package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Fine(
    val id: Int? = null,
    val residentId: String,
    val paymentId: Int? = null,
    val title: String,
    val description: String?,
    val amount: Float,
    val status: String,
    val resident: UserMinimal? = null,
    val residentialId: Int?,
    val createdAt: String?=null,
    val paymentPeriod: PaymentPeriod? = null
)