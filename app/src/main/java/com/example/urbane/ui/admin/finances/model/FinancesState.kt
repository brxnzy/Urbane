package com.example.urbane.ui.admin.finances.model

import com.example.urbane.data.model.Expense

data class FinancesState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: FinancesSuccess? = null,
    val expenses: List<Expense> = emptyList(),
    val amount: String = "",
    val description: String = "",
    val balance: Double = 0.0,
    val isLoadingBalance: Boolean = false
)

sealed class FinancesSuccess{
    object ExpenseRegistered : FinancesSuccess()
}

sealed class FinancesIntent {
    data class UpdateAmount(val amount: String) : FinancesIntent()
    data class UpdateDescription(val description: String) : FinancesIntent()
    object RegisterExpense : FinancesIntent()

}
    
