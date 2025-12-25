package com.example.urbane.data.model
import kotlinx.serialization.Serializable
@Serializable
data class InvoiceData(
    val invoiceId: String,
    val createdAt: String,
    val residentId: String?,
    val residentName: String?,
    val lines: List<TransactionDetail>,
    val totalAmount: Float,
    val totalPaid: Float,
    val totalRemaining: Float,
    val invoiceUrl: String? = null,
    val invoiceFileName: String? = null
)