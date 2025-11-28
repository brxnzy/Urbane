package com.example.urbane.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val createdAt: String? = null,
    val name: String,
    val photoUrl:String? = null,
    val idCard: String? = null,
    val active: Boolean? = null,
    val email: String? = null,
    val residential_id: Int? = null,
    val role_id: Int? = null,
    val role_name: String? = null,
    val residence_id: Int? = null,
    val residence_name: String? = null,
    val residence_type: String? = null,
    val residence_residential_id: Int? = null
)