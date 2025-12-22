package com.example.urbane.ui.admin.residences.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.ResidencesRepository
import com.example.urbane.ui.admin.residences.model.ResidencesIntent
import com.example.urbane.ui.admin.residences.model.ResidencesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.residences.model.ResidenceSuccessType


class ResidencesViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(ResidencesState())
    val state: StateFlow<ResidencesState> = _state.asStateFlow()
    val residencesRepository = ResidencesRepository(sessionManager)
    val userRepository = UserRepository(sessionManager)

    fun processIntent(intent: ResidencesIntent) {
        when (intent) {
            is ResidencesIntent.DescriptionChanged -> _state.update { it.copy(description = intent.description) }
            is ResidencesIntent.NameChanged -> _state.update { it.copy(name = intent.name) }
            is ResidencesIntent.TypeChanged -> _state.update { it.copy(type = intent.type) }
            is ResidencesIntent.OwnerNameChanged -> _state.update { it.copy(ownerName = intent.ownerName) }
            is ResidencesIntent.OwnerEmailChanged -> _state.update { it.copy(ownerEmail = intent.ownerEmail) }
            is ResidencesIntent.OwnerIdCardChanged -> _state.update { it.copy(ownerIdCard = intent.ownerIdCard) }
            is ResidencesIntent.OwnerPasswordChanged -> _state.update { it.copy(ownerPassword = intent.ownerPassword) }
            ResidencesIntent.CreateResidence -> createResidence()


            is ResidencesIntent.AssignPropietario -> {
                _state.update {
                    it.copy(
                        ownerName = intent.propietario.name,
                        ownerEmail = intent.propietario.email.toString(),
                        ownerIdCard = intent.propietario.idCard.toString(),
                        ownerId = intent.propietario.id.toString(),
                        selectedOwnerId = intent.propietario.id
                    )
                }
            }

            is ResidencesIntent.SelectOwner -> {
                _state.update { it.copy(selectedOwnerId = intent.ownerId) }
            }

            ResidencesIntent.ClearSelectedOwner -> {
                _state.update {
                    it.copy(
                        selectedOwnerId = null,
                        ownerName = "",
                        ownerEmail = "",
                        ownerIdCard = "",
                        ownerId = null
                    )
                }
            }
        }
    }

    private fun createResidence() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                residencesRepository.createResidence(
                    _state.value.name,
                    _state.value.type,
                    _state.value.description,
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = ResidenceSuccessType.ResidenceCreated,
                        name = "",
                        type = "",
                        description = "",
                        selectedOwnerId = null
                    )
                }

                loadResidences()

            } catch (e: Exception) {
                Log.e("ResidencesVM", "Error creando residencia: $e")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error desconocido al crear residencia"
                    )
                }
            }
        }
    }

    fun loadResidences() {
        viewModelScope.launch {

            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                val residences = residencesRepository.getResidences()

                Log.d("ResidencesVM", "Loaded ${residences.size} residences")

                _state.update {
                    it.copy(isLoading = false, residences = residences)
                }

            } catch (e: Exception) {
                Log.e("ResidencesVM", "Error loading residences: $e")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar residencias"
                    )
                }
            }
        }
    }

    fun clearSuccess() {
        _state.update { it.copy(success = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}






