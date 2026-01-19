package com.example.urbane.ui.admin.dashboard.model

import com.example.urbane.data.model.Incident
import com.example.urbane.data.model.Payment

data class DashboardState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val totalResidences: Int = 0,
    val occupiedResidences: Int = 0,
    val pendingPayments: List<Payment> = emptyList(),
    val recentIncidents: List<Incident> = emptyList()
)
