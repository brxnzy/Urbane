package com.example.urbane.ui.admin.settings.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.SurveyRepository
import com.example.urbane.ui.admin.settings.model.SurveysIntent
import com.example.urbane.ui.admin.settings.model.SurveysState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SurveysViewModel(sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(SurveysState())
    val state: StateFlow<SurveysState> = _state.asStateFlow()
    val repository = SurveyRepository(sessionManager)

    fun processIntent(intent: SurveysIntent) {
        when (intent) {
            SurveysIntent.ShowCreateSheet -> showCreateSheet()
            SurveysIntent.DismissBottomSheet -> dismissBottomSheet()
            is SurveysIntent.UpdateQuestion -> updateQuestion(intent.question)
            is SurveysIntent.UpdateOption -> updateOption(intent.index, intent.value)
            SurveysIntent.AddOption -> addOption()
            is SurveysIntent.RemoveOption -> removeOption(intent.index)
            SurveysIntent.CreateSurvey -> createSurvey()
        }
    }

    fun loadSurveys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val surveys = repository.getAllSurveys()
                _state.update {
                    it.copy(
                        surveys = surveys,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error desconocido al cargar encuestas"
                    )
                }
                Log.e("SurveysViewModel", "Error loading surveys: ${e.message}", e)
            }
        }
    }
    private fun showCreateSheet() {
        _state.update {
            it.copy(
                showBottomSheet = true,
                question = "",
                options = listOf("", ""),
                errorMessage = null
            )
        }
    }

    private fun dismissBottomSheet() {
        _state.update {
            it.copy(
                showBottomSheet = false,
                question = "",
                options = listOf("", ""),
                errorMessage = null
            )
        }
    }

    private fun updateQuestion(question: String) {
        _state.update {
            it.copy(
                question = question,
                canSave = validateForm(question, it.options)
            )
        }
    }

    private fun updateOption(index: Int, value: String) {
        val newOptions = _state.value.options.toMutableList()
        if (index in newOptions.indices) {
            newOptions[index] = value
            _state.update {
                it.copy(
                    options = newOptions,
                    canSave = validateForm(it.question, newOptions)
                )
            }
        }
    }

    private fun addOption() {
        val currentOptions = _state.value.options
        _state.update {
            it.copy(
                options = currentOptions + "",
                canSave = validateForm(it.question, currentOptions + "")
            )
        }
    }

    private fun removeOption(index: Int) {
        val newOptions = _state.value.options.toMutableList()
        if (newOptions.size > 2 && index in newOptions.indices) {
            newOptions.removeAt(index)
            _state.update {
                it.copy(
                    options = newOptions,
                    canSave = validateForm(it.question, newOptions)
                )
            }
        }
    }

    private fun validateForm(question: String, options: List<String>): Boolean {
        if (question.isBlank()) return false

        if (options.size < 2) return false

        if (options.any { it.isBlank() }) return false

        if (options.distinct().size != options.size) return false

        return true
    }

    private fun createSurvey() {
        if (!_state.value.canSave) {
            _state.update {
                it.copy(errorMessage = "Por favor completa todos los campos correctamente")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.createSurvey(
                    question = _state.value.question,
                    options = _state.value.options
                )
                loadSurveys()
                dismissBottomSheet()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al crear la encuesta"
                    )
                }
                Log.e("SurveysViewModel", "Error creating survey: ${e.message}", e)
            }
        }
    }
}