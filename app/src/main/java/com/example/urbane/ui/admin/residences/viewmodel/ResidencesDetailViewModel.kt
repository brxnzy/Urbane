package com.example.urbane.ui.admin.residences.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.ResidencesRepository
import com.example.urbane.ui.admin.residences.model.ResidencesDetailIntent
import com.example.urbane.ui.admin.residences.model.ResidencesDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class ResidencesDetailViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(ResidencesDetailState())
    val state = _state.asStateFlow()
    val residenceRepository = ResidencesRepository(sessionManager)

    fun processIntent(intent:ResidencesDetailIntent) {
        when (intent) {


            else -> {}

        }
    }


    fun loadResidence(id: Int){
        _state.update { it.copy(isLoading = true, residence =  null, errorMessage = null) }
        viewModelScope.launch {
            if (_state.value.residence != null) return@launch
            try {
                _state.update { it.copy(isLoading = true) }

                val residence = residenceRepository.getResidenceById(id)
                Log.d("ResidencesVM", "residencia por id cargada$residence")

                _state.update {
                    it.copy(isLoading = false, residence = residence , errorMessage = null)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }




    fun reset() {
        _state.value = ResidencesDetailState()
    }








}

