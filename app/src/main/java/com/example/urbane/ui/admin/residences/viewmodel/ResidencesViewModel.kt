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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.residences.model.SuccessType


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
            ResidencesIntent.CreateOwner -> createOwner()
            ResidencesIntent.LoadOwners -> loadOwners()

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
                        success = SuccessType.ResidenceCreated,
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

    fun loadOwners() {
        viewModelScope.launch {
            try {
                Log.d("ResidencesVM", "Loading owners...")
                _state.update { it.copy(isLoadingOwners = true, errorMessage = null) }

                val owners = userRepository.getOwners()

                Log.d("ResidencesVM", "Loaded ${owners.size} owners")

                _state.update {
                    it.copy(isLoadingOwners = false, availableOwners = owners)
                }

            } catch (e: Exception) {
                Log.e("ResidencesVM", "Error loading owners: $e")
                _state.update {
                    it.copy(
                        isLoadingOwners = false,
                        errorMessage = e.message ?: "Error al cargar propietarios"
                    )
                }
            }
        }
    }

    private fun createOwner() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                val user = userRepository.createUser(
                    _state.value.ownerName,
                    _state.value.ownerEmail,
                    _state.value.ownerIdCard,
                    _state.value.ownerPassword,
                    3, // roleId para propietario
                    _state.value.selectedResidence
                )

                if (user == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            success = SuccessType.PropietarioAssigned,
                            ownerName = "",
                            ownerEmail = "",
                            ownerIdCard = "",
                            ownerPassword = ""
                        )
                    }
                    loadOwners()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al crear propietario"
                        )
                    }
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al crear propietario"
                    )
                }
                Log.e("ResidencesVM", "Error creando propietario: $e")
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






