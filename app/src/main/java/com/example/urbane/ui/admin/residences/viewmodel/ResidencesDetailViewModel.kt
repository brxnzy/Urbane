package com.example.urbane.ui.admin.residences.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.ResidencesRepository
import com.example.urbane.ui.admin.residences.model.ResidencesDetailIntent
import com.example.urbane.ui.admin.residences.model.ResidencesDetailState
import com.example.urbane.ui.admin.residences.model.ResidencesDetailSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResidencesDetailViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(ResidencesDetailState())
    val state = _state.asStateFlow()
    val residenceRepository = ResidencesRepository(sessionManager)

    fun processIntent(intent: ResidencesDetailIntent) {
        when (intent) {
            is ResidencesDetailIntent.EditResidence -> {
                editResidence(
                    id = intent.id,
                    name = intent.name,
                    type = intent.type,
                    description = intent.description
                )
            }

            is ResidencesDetailIntent.DeleteResidence -> {
                deleteResidence(intent.id)
            }

            is ResidencesDetailIntent.VacateResidence -> {
                vacateResidence(
                    id = intent.id,
                    residentId = intent.residentId
                )
            }

            is ResidencesDetailIntent.UpdateName -> {
                _state.update { it.copy(editedName = intent.name) }
            }

            is ResidencesDetailIntent.UpdateType -> {
                _state.update { it.copy(editedType = intent.type) }
            }

            is ResidencesDetailIntent.UpdateDescription -> {
                _state.update { it.copy(editedDescription = intent.description) }
            }
        }
    }

    private fun editResidence(id: Int, name: String, type: String, description: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null, success = null) }
        viewModelScope.launch {
            try {
                residenceRepository.updateResidence(
                    id = id,
                    name = name,
                    type = type,
                    description = description
                )

                // Recargar la residencia actualizada
                val updatedResidence = residenceRepository.getResidenceById(id)

                _state.update {
                    it.copy(
                        isLoading = false,
                        residence = updatedResidence,
                        success = ResidencesDetailSuccess.ResidenceEdited,
                        errorMessage = null,
                        editedName = updatedResidence.name,
                        editedType = updatedResidence.type,
                        editedDescription = updatedResidence.description,
                        originalName = updatedResidence.name,
                        originalType = updatedResidence.type,
                        originalDescription = updatedResidence.description
                    )
                }

                Log.d("ResidencesVM", "Residencia editada exitosamente")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al editar residencia: ${e.message}"
                    )
                }
                Log.e("ResidencesVM", "Error al editar residencia: $e")
            }
        }
    }

    private fun deleteResidence(id: Int) {
        _state.update { it.copy(isLoading = true, errorMessage = null, success = null) }
        viewModelScope.launch {
            try {
                Log.d("ResidencesVM", "intentando borrar residencia con el id $id")
                residenceRepository.deleteResidence(id)

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = ResidencesDetailSuccess.ResidenceDeleted,
                        errorMessage = null
                    )
                }

                Log.d("ResidencesVM", "Residencia eliminada exitosamente")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al eliminar residencia: ${e.message}"
                    )
                }
                Log.e("ResidencesVM", "Error al eliminar residencia: $e")
            }
        }
    }

    private fun vacateResidence(id: Int, residentId: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null, success = null) }
        viewModelScope.launch {
            try {
                val updatedResidence = residenceRepository.vacateResidence(id, residentId)

                _state.update {
                    it.copy(
                        isLoading = false,
                        residence = updatedResidence,
                        success = ResidencesDetailSuccess.ResidenceVacated,
                        errorMessage = null
                    )
                }

                Log.d("ResidencesVM", "Residencia desalojada exitosamente")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al desalojar residencia: ${e.message}"
                    )
                }
                Log.e("ResidencesVM", "Error al desalojar residencia: $e")
            }
        }
    }

    fun loadResidence(id: Int) {
        _state.update { it.copy(isLoading = true, residence = null, errorMessage = null) }
        viewModelScope.launch {
            if (_state.value.residence != null) return@launch
            try {
                _state.update { it.copy(isLoading = true) }

                val residence = residenceRepository.getResidenceById(id)
                Log.d("ResidencesVM", "Residencia por id cargada: $residence")

                _state.update {
                    it.copy(
                        isLoading = false,
                        residence = residence,
                        errorMessage = null,
                        editedName = residence.name,
                        editedType = residence.type,
                        editedDescription = residence.description,
                        originalName = residence.name,
                        originalType = residence.type,
                        originalDescription = residence.description
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetSuccess() {
        _state.update{it.copy(success = null )}
    }
}