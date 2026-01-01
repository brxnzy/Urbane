
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinancesRepository
import com.example.urbane.ui.admin.finances.model.FinancesIntent
import com.example.urbane.ui.admin.finances.model.FinancesState
import com.example.urbane.ui.admin.finances.model.FinancesSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinancesViewModel(sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(FinancesState())
    val state = _state.asStateFlow()
    private val repository = FinancesRepository(sessionManager)

    fun handleIntent(intent: FinancesIntent) {
        when (intent) {
            is FinancesIntent.UpdateAmount -> {
                _state.value = _state.value.copy(amount = intent.amount)
            }
            is FinancesIntent.UpdateDescription -> {
                _state.value = _state.value.copy(description = intent.description)
            }
            is FinancesIntent.RegisterExpense -> registerExpense()

            // Nuevos intents para reporte financiero
            is FinancesIntent.UpdateStartDate -> {
                _state.value = _state.value.copy(startDate = intent.date)
                // Si ambas fechas están seleccionadas, cargar reporte automáticamente
                if (_state.value.endDate != null && intent.date != null) {
                    loadReport()
                }
            }
            is FinancesIntent.UpdateEndDate -> {
                _state.value = _state.value.copy(endDate = intent.date)
                // Si ambas fechas están seleccionadas, cargar reporte automáticamente
                if (_state.value.startDate != null && intent.date != null) {
                    loadReport()
                }
            }
            is FinancesIntent.GeneratePDF -> generatePDF()
            else -> {}
        }
    }

    private fun registerExpense() {
        viewModelScope.launch {
            try {
                val amount = _state.value.amount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _state.value = _state.value.copy(
                        errorMessage = "Ingrese un monto válido"
                    )
                    return@launch
                }

                if (_state.value.description.isBlank()) {
                    _state.value = _state.value.copy(
                        errorMessage = "Ingrese una descripción"
                    )
                    return@launch
                }

                if (amount > _state.value.balance) {
                    _state.value = _state.value.copy(
                        errorMessage = "El monto excede el balance disponible"
                    )
                    return@launch
                }

                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                repository.registerExpense(amount, _state.value.description)

                _state.value = _state.value.copy(
                    isLoading = false,
                    success = FinancesSuccess.ExpenseRegistered,
                    amount = "",
                    description = ""
                )

                loadExpenses()
                loadBalance()
            } catch (e: Exception) {
                Log.e("FinancesViewModel", "Error registrando egreso: $e")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al registrar egreso: ${e.message}"
                )
            }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            try {
                val startDate = _state.value.startDate
                val endDate = _state.value.endDate

                if (startDate == null || endDate == null) {
                    return@launch
                }

                if (startDate > endDate) {
                    _state.value = _state.value.copy(
                        errorMessage = "La fecha de inicio debe ser anterior a la fecha de fin"
                    )
                    return@launch
                }

                _state.value = _state.value.copy(
                    isLoadingReport = true,
                    errorMessage = null
                )

                // Cargar transacciones del rango de fechas
                val transactions = repository.getTransactionsByDateRange(startDate, endDate)

                // Calcular totales
                val totalIngresos = transactions
                    .filter { it.type == com.example.urbane.ui.admin.finances.model.TransactionType.INGRESO }
                    .sumOf { it.amount }

                val totalEgresos = transactions
                    .filter { it.type == com.example.urbane.ui.admin.finances.model.TransactionType.EGRESO }
                    .sumOf { it.amount }

                _state.value = _state.value.copy(
                    isLoadingReport = false,
                    filteredTransactions = transactions,
                    totalIngresos = totalIngresos,
                    totalEgresos = totalEgresos,
                    success = FinancesSuccess.ReportGenerated
                )

            } catch (e: Exception) {
                Log.e("FinancesViewModel", "Error cargando reporte: $e")
                _state.value = _state.value.copy(
                    isLoadingReport = false,
                    errorMessage = "Error al cargar reporte: ${e.message}"
                )
            }
        }
    }

    private fun generatePDF() {
        // TODO: Implementar generación de PDF
        Log.d("FinancesViewModel", "Generar PDF - Función pendiente de implementación")
        // Aquí irá la lógica para generar el PDF del reporte
    }

    fun loadExpenses() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                val expenses = repository.getAllExpenses()
                _state.value = _state.value.copy(
                    isLoading = false,
                    expenses = expenses
                )
            } catch (e: Exception) {
                Log.e("FinancesViewModel", "Error cargando egresos: $e")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar egresos: ${e.message}"
                )
            }
        }
    }

    fun loadBalance() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingBalance = true)
                val balance = repository.getBalance()
                _state.value = _state.value.copy(
                    isLoadingBalance = false,
                    balance = balance
                )
            } catch (e: Exception) {
                Log.e("FinancesViewModel", "Error cargando balance: $e")
                _state.value = _state.value.copy(isLoadingBalance = false)
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(success = null)
    }
}