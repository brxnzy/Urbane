package com.example.urbane.data.model

import kotlinx.serialization.Serializable


@Serializable
data class InvoiceData(
    val invoiceId: String,                    // puede ser UUID o timestamp
    val createdAt: String,
    val residentId: String?,
    val residentName: String?,
    val lines: List<TransactionDetail>,
    val totalAmount: Float,                   // suma de paymentAmount (originales) o suma informativa
    val totalPaid: Float,                     // suma de transactionAmount (lo que se pagó ahora)
    val totalRemaining: Float,
    val invoiceUrl: String? = null// suma de remaining después de aplicar pagos
)