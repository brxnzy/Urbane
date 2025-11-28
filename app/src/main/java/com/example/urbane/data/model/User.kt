package com.example.urbane.data.model

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: String,
    val email: String? = null,
    val name: String,
    val idCard:String,
    val phone: String? = null,
    val apartment: String? = null,
    val createdAt: String
)