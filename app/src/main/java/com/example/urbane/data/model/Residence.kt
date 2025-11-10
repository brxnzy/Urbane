package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Residence(
    val name: String,
    val type: String,
    val description: String,
    val residentialId: Int,
    val ownerId: String? = null ,
    val residentId:String? = null
)
