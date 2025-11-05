package com.example.urbane.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Role(
    val id:Int,
    val name: String
)
