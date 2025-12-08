package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentTransaction(
    val id: Int? = null,
    val paymentId: Int,
    val amount: Float,
    val method: String,
    val residentialId: Int? = null,
    val createdAt: String? = null


)


