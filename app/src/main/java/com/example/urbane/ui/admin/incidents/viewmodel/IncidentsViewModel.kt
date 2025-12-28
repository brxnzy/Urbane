package com.example.urbane.ui.admin.incidents.viewmodel
import androidx.lifecycle.ViewModel
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.admin.incidents.model.IncidentsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IncidentsViewModel(sessionManager: SessionManager): ViewModel() {
    private val _state = MutableStateFlow(IncidentsState())
    val state = _state.asStateFlow()
}