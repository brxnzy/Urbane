package com.example.urbane.ui.admin.incidents.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.IncidentsRepository
import com.example.urbane.ui.admin.incidents.model.IncidentsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IncidentsViewModel(sessionManager: SessionManager): ViewModel() {
    private val _state = MutableStateFlow(IncidentsState())
    val state = _state.asStateFlow()
    val repository = IncidentsRepository(sessionManager)


    fun loadIncidents(){
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val categories = repository.getIncidentCategories()
                val incidents = repository.getIncidents()

                _state.update { it.copy(isLoading = false, categories = categories, incidents = incidents) }

            }catch (e: Exception){
                Log.e("IncidentsViewModel", "error obteniendo las incidencias $e")
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}