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