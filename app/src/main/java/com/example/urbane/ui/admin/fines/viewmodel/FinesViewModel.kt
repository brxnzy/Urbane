package com.example.urbane.ui.admin.fines.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinesRepository
import com.example.urbane.ui.admin.fines.model.FinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FinesViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(FinesState())
    val state: StateFlow<FinesState> = _state

    val finesRepository = FinesRepository(sessionManager)


    fun loadFines() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val fines = finesRepository.getAllFines()

                _state.update {
                    it.copy(
                        isLoading = false,
                        fines = fines
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error cargando multas"
                    )
                }
            }
        }
    }
}