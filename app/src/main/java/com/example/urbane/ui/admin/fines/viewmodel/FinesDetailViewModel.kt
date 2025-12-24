package com.example.urbane.ui.admin.fines.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinesRepository
import com.example.urbane.data.repository.PaymentRepository
import com.example.urbane.ui.admin.fines.model.FineDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FinesDetailViewModel(
    sessionManager: SessionManager
) : ViewModel() {

    private val repository = FinesRepository(sessionManager)

    private val _state = MutableStateFlow(FineDetailState())
    val state = _state.asStateFlow()

    fun loadFine(fineId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                val fine = repository.getFineById(fineId)



                _state.update {
                    it.copy(
                        isLoading = false,
                        fine = fine
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }



}
