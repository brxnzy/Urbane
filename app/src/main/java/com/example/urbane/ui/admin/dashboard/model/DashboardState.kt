package com.example.urbane.ui.admin.dashboard.model

data class DashboardState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
)
