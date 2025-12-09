package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDetail(
    val transactionId: Int? = null,
    val transactionAmount: Float? = null,
    val paymentId: Int? = null,
    val residentId: String? = null,
    val month: Int? = null,
    val year: Int? = null,
    val paymentStatus: String? = null,
    val paymentAmount: Float? = null,
    val paidAmount: Float? = null,
    val residentName: String? = null,
    val contractAmount: Float? = null,
    val services: List<Service>? = emptyList()
)
