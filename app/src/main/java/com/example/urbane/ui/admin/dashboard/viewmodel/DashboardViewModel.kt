package com.example.urbane.ui.admin.dashboard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinancesRepository
import com.example.urbane.data.repository.IncidentsRepository
import com.example.urbane.data.repository.PaymentRepository
import com.example.urbane.data.repository.ResidencesRepository
import com.example.urbane.ui.admin.dashboard.model.DashboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state
    val financesRepository = FinancesRepository(sessionManager)
    val residencesRepository = ResidencesRepository(sessionManager)


    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val balance = financesRepository.getBalance()
                val income = financesRepository.getIncomesByMonth()
                val expense = financesRepository.getExpensesByMonth()
                val residences = residencesRepository.getResidences()
                val totalResidences = residences.size
                val occupiedResidences = residences.count { it.available == false }
                val pendingPayments = PaymentRepository(sessionManager).pendingPayments()
                val recentIncidents = IncidentsRepository(sessionManager).getIncidents().take(2)

                _state.update {
                    it.copy(isLoading = false,balance = balance, income = income, expense = expense, totalResidences = totalResidences, occupiedResidences = occupiedResidences, pendingPayments = pendingPayments ,  recentIncidents = recentIncidents,errorMessage = null)
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error cargando datos del dashboard", e)
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

