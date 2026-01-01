package com.example.urbane.ui.admin.finances.model

import com.example.urbane.data.model.Expense
import com.example.urbane.data.model.Transaction

data class FinancesState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: FinancesSuccess? = null,
    val expenses: List<Expense> = emptyList(),
    val amount: String = "",
    val description: String = "",
    val balance: Double = 0.0,
    val isLoadingBalance: Boolean = false,

    val startDate: String? = null,
    val endDate: String? = null,
    val filteredTransactions: List<Transaction> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalEgresos: Double = 0.0,
    val isLoadingReport: Boolean = false
)

enum class TransactionType {
    INGRESO,
    EGRESO
}
sealed class FinancesSuccess {
    object ExpenseRegistered : FinancesSuccess()
    object ReportGenerated : FinancesSuccess()
}
sealed class FinancesIntent {
    data class UpdateAmount(val amount: String) : FinancesIntent()
    data class UpdateDescription(val description: String) : FinancesIntent()
    object RegisterExpense : FinancesIntent()
    data class UpdateStartDate(val date: String?) : FinancesIntent()
    data class UpdateEndDate(val date: String?) : FinancesIntent()
    object GenerateReport : FinancesIntent()
    object GeneratePDF : FinancesIntent()
}