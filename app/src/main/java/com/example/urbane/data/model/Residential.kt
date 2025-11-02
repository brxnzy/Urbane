package com.example.urbane.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Residential(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val logoUrl: String
)
