package com.example.urbane.data.model

import com.example.urbane.ui.admin.finances.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int,
    val type: TransactionType = TransactionType.INGRESO,
    val amount: Double,
    val description: String?=null,
    val date: String,
    val createdBy: String? = null
)
