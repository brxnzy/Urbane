package com.example.urbane.ui.admin.incidents.model

data class IncidentsState(
    val isLoading :Boolean = false,
//    val incidents : List<Incident> = emptyList(),
    val errorMessage : String? = null,
    val success : Boolean? = null,
)