package com.example.urbane.ui.admin.contracts.viewmodel

import androidx.lifecycle.ViewModel
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.ContractsRepository
import com.example.urbane.ui.admin.contracts.model.ContractsState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContractsViewModel(val sessionManager: SessionManager) : ViewModel() {
    private val contractsRepository = ContractsRepository(sessionManager)

    private val _state = MutableStateFlow(ContractsState())
    val state: StateFlow<ContractsState> = _state.asStateFlow()

    fun loadContracts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val contracts = contractsRepository.getContracts()
                _state.update { it.copy(
                        contracts = contracts,
                        isLoading = false,
                        error = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }


}

