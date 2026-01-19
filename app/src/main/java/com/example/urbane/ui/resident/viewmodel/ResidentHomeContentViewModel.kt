package com.example.urbane.ui.resident.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.SurveyRepository
import com.example.urbane.ui.resident.model.ResidentHomeIntent
import com.example.urbane.ui.resident.model.ResidentHomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResidentHomeContentViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ResidentHomeState())
    val state: StateFlow<ResidentHomeState> = _state.asStateFlow()

    private val surveyRepository = SurveyRepository(sessionManager)

    init {
        loadAvailableSurveys()
    }

    fun processIntent(intent: ResidentHomeIntent) {
        when (intent) {
            is ResidentHomeIntent.SelectSurveyOption -> {
                selectSurveyOption(intent.surveyId, intent.optionId)
            }
            is ResidentHomeIntent.VoteSurvey -> {
                voteSurvey(intent.surveyId)
            }
        }
    }

    fun loadAvailableSurveys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingSurveys = true, surveysError = null) }
            try {
                val surveys = surveyRepository.showAvailableSurvey()
                _state.update {
                    it.copy(
                        availableSurveys = surveys,
                        isLoadingSurveys = false
                    )
                }
                Log.d("ResidentHomeVM", "Loaded ${surveys.size} available surveys")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingSurveys = false,
                        surveysError = e.message ?: "Error al cargar encuestas"
                    )
                }
                Log.e("ResidentHomeVM", "Error loading surveys: ${e.message}", e)
            }
        }
    }
    private fun selectSurveyOption(surveyId: Int, optionId: Int) {
        _state.update {
            val newSelectedOptions = it.selectedOptions.toMutableMap()
            newSelectedOptions[surveyId] = optionId
            it.copy(selectedOptions = newSelectedOptions)
        }
        Log.d("ResidentHomeVM", "Selected option $optionId for survey $surveyId")
    }

    private fun voteSurvey(surveyId: Int) {
        val selectedOptionId = _state.value.selectedOptions[surveyId]
        if (selectedOptionId == null) {
            Log.w("ResidentHomeVM", "No option selected for survey $surveyId")
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingSurveys = true) }


                surveyRepository.voteSurvey(surveyId, selectedOptionId)

                Log.d("ResidentHomeVM", "Vote registered successfully")

                loadAvailableSurveys()

                _state.update {
                    val newSelectedOptions = it.selectedOptions.toMutableMap()
                    newSelectedOptions.remove(surveyId)
                    it.copy(selectedOptions = newSelectedOptions)
                }

            } catch (e: Exception) {
                Log.e("ResidentHomeVM", "Error voting: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoadingSurveys = false,
                        surveysError = e.message ?: "Error al votar"
                    )
                }
            }
        }
    }
}