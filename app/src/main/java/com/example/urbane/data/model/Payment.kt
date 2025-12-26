package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: Int?,
    val residentId: String,
    val month: Int,
    val year: Int,
    val amount: Float,
    val paidAmount: Float,
    val status: String,
    val createdAt: String,
    val paymentTransactions: List<PaymentTransaction> = emptyList(),
    val fines: List<Fine> = emptyList(),
    val resident: UserMinimal? = null
)

