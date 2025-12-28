package com.example.urbane.ui.admin.incidents.model
import com.example.urbane.data.model.Incident
import com.example.urbane.data.model.IncidentCategory

data class IncidentsState(
    val isLoading :Boolean = false,
    val errorMessage : String? = null,
    val success : Boolean? = null,
    val categories : List<IncidentCategory> = emptyList(),
    val incidents : List<Incident> = emptyList()
)