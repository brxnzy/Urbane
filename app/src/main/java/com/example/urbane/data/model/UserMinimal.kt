package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserMinimal(
    val id: String,
    val name: String ,
    val photoUrl:String? = null,

)
