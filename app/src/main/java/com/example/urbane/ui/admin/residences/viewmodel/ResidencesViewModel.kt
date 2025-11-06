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



class ResidencesViewModel(private val sessionManager: SessionManager) : ViewModel(){
    private val _state = MutableStateFlow(ResidencesState())
    val state: StateFlow<ResidencesState> = _state.asStateFlow()

    fun processIntent(intent: ResidencesIntent){
        when(intent){
            is ResidencesIntent.DescriptionChanged -> {
                _state.update { it.copy(description = intent.description) }
            }
            is ResidencesIntent.NameChanged -> {
                _state.update{it.copy(name = intent.name)}
            }
            is ResidencesIntent.TypeChanged -> {
                _state.update{it.copy(type = intent.type)}
            }

            ResidencesIntent.CreateResidence -> createResidence()
        }
    }
    private fun createResidence() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val user = sessionManager.sessionFlow.firstOrNull()
                    ?: throw IllegalStateException("No hay sesión activa")

                val residentialId = user.userData?.residential?.id
                    ?: throw IllegalStateException("No se encontró el ID del residencial")

                ResidencesRepository().createResidence(_state.value.name,_state.value.type,_state.value.description, residentialId)

                _state.update { it.copy(isLoading = false, success = true, name = "", type = "", description = "") }


            } catch (e: Exception) {
                Log.e("ResidencesVM","error creando residencias $e")
                _state.update{it.copy(errorMessage = e.toString())}

            }
        }
    }

    fun loadResidences() {
        viewModelScope.launch {
            if (_state.value.residences.isNotEmpty()) return@launch // 👈 evita recargar
            try {
                _state.update { it.copy(isLoading = true) }

                val user = sessionManager.sessionFlow.firstOrNull()
                    ?: throw IllegalStateException("No hay sesión activa")

                val residentialId = user.userData?.residential?.id
                    ?: throw IllegalStateException("No se encontró el ID del residencial")

                val residences = ResidencesRepository().getResidences(residentialId)

                _state.update {
                    it.copy(isLoading = false, residences = residences, errorMessage = null)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }



}





