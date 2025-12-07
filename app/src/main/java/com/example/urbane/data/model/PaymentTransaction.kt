package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentTransaction(
    val id: String? = null,
    val paymentId: Int,
    val amount: Float,
    val method: String,
)