package com.example.urbane.ui.admin.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.AuditLogsRepository
import com.example.urbane.ui.admin.settings.model.AuditLogsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuditLogsViewModel(
   sessionManager: SessionManager
) : ViewModel() {
    val repository = AuditLogsRepository(sessionManager)


    private val _state = MutableStateFlow(AuditLogsState())
    val state: StateFlow<AuditLogsState> = _state


    fun loadLogs() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val logs = repository.getAllLogs()

                _state.update {
                    it.copy(
                        isLoading = false,
                        logs = logs
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}
