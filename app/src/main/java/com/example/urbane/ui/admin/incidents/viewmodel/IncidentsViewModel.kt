package com.example.urbane.ui.admin.incidents.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.IncidentsRepository
import com.example.urbane.ui.admin.incidents.model.IncidentsIntent
import com.example.urbane.ui.admin.incidents.model.IncidentsState
import com.example.urbane.ui.admin.incidents.model.IncidentsSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IncidentsViewModel(sessionManager: SessionManager): ViewModel() {
    private val _state = MutableStateFlow(IncidentsState())
    val state = _state.asStateFlow()
    val repository = IncidentsRepository(sessionManager)

    fun handleIntent(intent: IncidentsIntent) {
        when (intent) {
            is IncidentsIntent.SelectIncident -> selectIncident(intent.incident)
            is IncidentsIntent.UpdateScheduledDate -> updateScheduledDate(intent.date)
            is IncidentsIntent.UpdateStartTime -> updateStartTime(intent.time)
            is IncidentsIntent.UpdateAdminResponse -> updateAdminResponse(intent.response)
            is IncidentsIntent.AttendIncident -> attendIncident()
            is IncidentsIntent.ClearSelection -> clearSelection()
            is IncidentsIntent.RejectIncident -> rejectIncident(intent.id)
            else -> {}
        }
    }

    fun loadIncidents(){
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val categories = repository.getIncidentCategories()
                val incidents = repository.getIncidents()

                _state.update { it.copy(isLoading = false, categories = categories, incidents = incidents) }

            } catch (e: Exception){
                Log.e("IncidentsViewModel", "error obteniendo las incidencias $e")
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun rejectIncident(id: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(errorMessage = null) }
                repository.rejectIncident(id)
                _state.update { it.copy(success = IncidentsSuccess.IncidentRejected) }
                loadIncidents()
            } catch (e: Exception) {
                Log.e("IncidentsViewModel", "Error al rechazar la incidencia: $e")
                _state.update { it.copy(errorMessage = "Error al rechazar la incidencia: ${e.message}") }

            }
        }
    }

        private fun selectIncident(incident: com.example.urbane.data.model.Incident) {
            _state.update {
                it.copy(
                    selectedIncident = incident,
                    scheduledDate = "",
                    startTime = "",
                    adminResponse = ""
                )
            }
        }

        private fun clearSelection() {
            _state.update {
                it.copy(
                    selectedIncident = null,
                    scheduledDate = "",
                    startTime = "",
                    adminResponse = "",
                    errorMessage = null
                )
            }
        }

        private fun updateScheduledDate(date: String) {
            _state.update { it.copy(scheduledDate = date) }
        }

        private fun updateStartTime(time: String) {
            _state.update { it.copy(startTime = time) }
        }

        private fun updateAdminResponse(response: String) {
            _state.update { it.copy(adminResponse = response) }
        }

        private fun attendIncident() {
            viewModelScope.launch {
                try {
                    val currentState = _state.value
                    val incident = currentState.selectedIncident ?: return@launch

                    if (currentState.scheduledDate.isEmpty() ||
                        currentState.startTime.isEmpty() ||
                        currentState.adminResponse.isEmpty()
                    ) {
                        _state.update { it.copy(errorMessage = "Todos los campos son requeridos") }
                        return@launch
                    }

                    _state.update { it.copy(isProcessing = true, errorMessage = null) }

                    repository.attendIncident(
                        incidentId = incident.id!!,
                        scheduledDate = currentState.scheduledDate,
                        startTime = currentState.startTime,
                        adminResponse = currentState.adminResponse
                    )

                    _state.update {
                        it.copy(
                            isProcessing = false,
                            selectedIncident = null,
                            success = IncidentsSuccess.IncidentAttended
                        )
                    }

                    loadIncidents()

                } catch (e: Exception) {
                    Log.e("IncidentsViewModel", "Error atendiendo incidencia: $e")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Error al atender la incidencia: ${e.message}"
                        )
                    }
                }
            }
        }

    fun resetSuccess(){
        _state.update { it.copy(success = null) }
    }
    }