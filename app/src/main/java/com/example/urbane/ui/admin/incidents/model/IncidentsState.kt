package com.example.urbane.ui.admin.incidents.model
import com.example.urbane.data.model.Incident
import com.example.urbane.data.model.IncidentCategory

data class IncidentsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean? = null,
    val categories: List<IncidentCategory> = emptyList(),
    val incidents: List<Incident> = emptyList(),

    // Campos para atender incidencia
    val selectedIncident: Incident? = null,
    val scheduledDate: String = "",
    val startTime: String = "",
    val adminResponse: String = "",
    val isProcessing: Boolean = false
    // Removemos showAttendDialog
)


sealed class IncidentsIntent {
    data class SelectIncident(val incident: Incident) : IncidentsIntent()
    data class UpdateScheduledDate(val date: String) : IncidentsIntent()
    data class UpdateStartTime(val time: String) : IncidentsIntent()
    data class UpdateAdminResponse(val response: String) : IncidentsIntent()
    data object AttendIncident : IncidentsIntent()
    data object ClearSelection : IncidentsIntent()
}