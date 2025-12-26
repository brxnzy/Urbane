package com.example.urbane.data.model

import kotlinx.serialization.Serializable

    @Serializable
    data class Service(
        val id: Int,
        val name: String,
        val price: Float,
    )