package com.example.urbane.data.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AuditLog(
    val id: Int? = null,
    val adminId: String,
    val admin: UserMinimal? = null,
    val action: String,
    val entity: String,
    val entityId: String? = null,
    val data: JsonObject? = null,
    val residentialId: Int,
    val createdAt: String? = null
)
