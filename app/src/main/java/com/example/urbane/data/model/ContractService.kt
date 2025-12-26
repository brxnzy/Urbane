package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ContractService(
    val id: Int,
    val createdAt: String,
    val contractId: Int,
    val serviceId: Int,
    val name: String,
    val price: Double
)
