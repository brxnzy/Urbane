import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.FinancesRepository
import com.example.urbane.ui.admin.finances.model.FinancesIntent
import com.example.urbane.ui.admin.finances.model.FinancesState
import com.example.urbane.ui.admin.finances.model.FinancesSuccess
import com.example.urbane.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinancesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {
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

            is FinancesIntent.UpdateStartDate -> {
                _state.value = _state.value.copy(startDate = intent.date)
                if (_state.value.endDate != null && intent.date != null) {
                    loadReport()
                }
            }
            is FinancesIntent.UpdateEndDate -> {
                _state.value = _state.value.copy(endDate = intent.date)
                if (_state.value.startDate != null && intent.date != null) {
                    loadReport()
                }
            }
            is FinancesIntent.GeneratePDF -> generatePDF(intent.context)
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

                val transactions = repository.getTransactionsByDateRange(startDate, endDate)

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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun generatePDF(context: Context) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                val currentState = _state.value

                if (currentState.startDate == null || currentState.endDate == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Seleccione un rango de fechas"
                    )
                    return@launch
                }

                if (currentState.filteredTransactions.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No hay transacciones para generar el reporte"
                    )
                    return@launch
                }

                // Generar PDF en hilo de background
                val (pdfUri, fileName) = PdfGenerator.generateFinancialReport(
                    context = context,
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    transactions = currentState.filteredTransactions,
                    totalIngresos = currentState.totalIngresos,
                    totalEgresos = currentState.totalEgresos
                )

                if (pdfUri != null && fileName != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = FinancesSuccess.PDFGenerated(pdfUri, fileName)
                    )
                    Log.d("FinancesViewModel", "PDF generado exitosamente: $fileName")
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error al generar el PDF"
                    )
                }

            } catch (e: Exception) {
                Log.e("FinancesViewModel", "Error generando PDF: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error al generar PDF: ${e.message}"
                )
            }
        }
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