package com.example.urbane.ui.admin.settings.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residential
import com.example.urbane.data.repository.ResidentialRepository
import com.example.urbane.ui.admin.settings.model.ResidentialIntent
import com.example.urbane.ui.admin.settings.model.ResidentialState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResidentialViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val repository = ResidentialRepository(sessionManager)

    private val _state = MutableStateFlow(ResidentialState())
    val state: StateFlow<ResidentialState> = _state.asStateFlow()

    init {
        loadResidentials()
    }

    fun processIntent(intent: ResidentialIntent) {
        when (intent) {
            ResidentialIntent.LoadResidentials -> loadResidentials()
            ResidentialIntent.DismissBottomSheet -> dismissBottomSheet()
            ResidentialIntent.ShowCreateSheet -> showCreateSheet()
            is ResidentialIntent.ShowEditSheet -> showEditSheet(intent.residential)
            is ResidentialIntent.UpdateResidential -> updateResidential(intent.residential)
            is ResidentialIntent.CreateResidential -> createResidential(
                intent.name, intent.address, intent.phone, intent.logoUrl
            )
            is ResidentialIntent.RemoveResidential -> removeResidential(intent.residentialId)
            ResidentialIntent.DismissError -> dismissError()
        }
    }

    private fun loadResidentials() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                val residentials = repository.getUserResidentials()
                _state.update {
                    it.copy(
                        residentials = residentials,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ResidentialsManagementVM", "Error: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar residenciales: ${e.message}"
                    )
                }
            }
        }
    }

    private fun showEditSheet(residential: Residential) {
        _state.update {
            it.copy(
                showBottomSheet = true,
                isEditMode = true,
                selectedResidential = residential
            )
        }
    }

    private fun showCreateSheet() {
        _state.update {
            it.copy(
                showBottomSheet = true,
                isEditMode = false,
                selectedResidential = null
            )
        }
    }

    private fun dismissBottomSheet() {
        _state.update {
            it.copy(
                showBottomSheet = false,
                selectedResidential = null
            )
        }
    }

    private fun updateResidential(residential: Residential) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val success = repository.updateResidential(residential)

                if (success) {
                    _state.update { it.copy(showBottomSheet = false, selectedResidential = null) }
                    loadResidentials() // Recargar lista
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al actualizar residencial"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun createResidential(name: String, address: String?, phone: String?, logoUrl: String?) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val success = repository.createResidential(name, address, phone, logoUrl)

                if (success) {
                    _state.update { it.copy(showBottomSheet = false) }
                    loadResidentials()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al crear residencial"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun removeResidential(residentialId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val success = repository.removeResidentialAssignment(residentialId)

                if (success) {
                    loadResidentials()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar asignación"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}