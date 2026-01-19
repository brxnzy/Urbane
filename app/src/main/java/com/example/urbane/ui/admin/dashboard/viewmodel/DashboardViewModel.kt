package com.example.urbane.ui.admin.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinancesRepository
import com.example.urbane.ui.admin.dashboard.model.DashboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state
    val financesRepository = FinancesRepository(sessionManager)


    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val balance = financesRepository.getBalance()
                val income = financesRepository.getIncomesByMonth()
                val expense = financesRepository.getExpensesByMonth()


                _state.update {
                    it.copy(isLoading = false,balance = balance, income = income, expense = expense, errorMessage = null)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

