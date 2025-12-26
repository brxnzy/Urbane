package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentPeriod(
    val month: Int,
    val year: Int
)
