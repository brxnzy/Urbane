package com.example.urbane.ui.admin.fines.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinesRepository
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.fines.model.FinesIntent
import com.example.urbane.ui.admin.fines.model.FinesState
import com.example.urbane.ui.admin.fines.model.FinesSuccessType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FinesViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(FinesState())
    val state: StateFlow<FinesState> = _state

    val finesRepository = FinesRepository(sessionManager)


    fun handleIntent(intent: FinesIntent) {
        when (intent) {

            is FinesIntent.TitleChanged ->
                _state.update { it.copy(title = intent.value) }

            is FinesIntent.DescriptionChanged ->
                _state.update { it.copy(description = intent.value) }

            is FinesIntent.AmountChanged ->
                _state.update { it.copy(amount = intent.value) }

            is FinesIntent.ResidentSelected ->
                _state.update { it.copy(selectedResidentId = intent.residentId) }

            FinesIntent.CreateFine ->
                createFine()

            FinesIntent.ClearSuccess ->
                _state.update { it.copy(success = null) }
        }
    }




    fun loadFines() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

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
                        errorMessage = e.message ?: "Error cargando multas"
                    )
                }
            }
        }
    }

    fun loadResidents() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val residents = UserRepository(sessionManager).getResidents()
                _state.update {
                    it.copy(
                        residents = residents,
                        isLoading = false
                    )
                }
                Log.d("FinesViewModel", "Residentes cargados: ${residents.size}")
            } catch (e: Exception) {
                Log.e("FinesViewModel", "Error loading residents: $e")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar residentes"
                    )
                }
            }
        }
    }

    private fun createFine() {
        viewModelScope.launch {
            try {
                val currentState = _state.value

                val residentId = currentState.selectedResidentId
                    ?: error("No resident selected")

                _state.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        success = null
                    )
                }

                finesRepository.createFine(
                    residentId = residentId,
                    title = currentState.title.trim(),
                    description = currentState.description.trim().ifBlank { null },
                    amount = currentState.amount.trim()
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = FinesSuccessType.FineCreated,
                        title = "",
                        description = "",
                        amount = "",
                        selectedResidentId = null
                    )
                }

                loadFines()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error creando multa"
                    )
                }
            }
        }
    }


}