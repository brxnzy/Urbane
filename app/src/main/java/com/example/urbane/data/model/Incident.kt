package com.example.urbane.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Incident(
    val id: Int? = null,
    val createdAt: String? = null,
    val title: String = "",
    val description: String = "",
    val status: String? = null,
    @SerialName("type")
    val category: String? = null,
    val residentId: String = "",
    val residentName: Map<String,String>,
    val residentialId: Int,
    val imageUrls: List<String>? = null,
    val scheduledDate: String? = null,
    val startTime: String? = null,
)
