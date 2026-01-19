package com.example.urbane.ui.admin.payments.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.User
import com.example.urbane.data.repository.PaymentRepository
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.payments.model.PaymentSuccess
import com.example.urbane.ui.admin.payments.model.PaymentsIntent
import com.example.urbane.ui.admin.payments.model.PaymentsState
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class PaymentsViewModel(
    val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentsState())
    val state: StateFlow<PaymentsState> = _state.asStateFlow()

    private val paymentRepository = PaymentRepository(sessionManager)
    private val userRepository = UserRepository(sessionManager)

    // Manejador de intents
    fun handleIntent(intent: PaymentsIntent) {
        when (intent) {
            is PaymentsIntent.SelectResident -> {
                selectResident(intent.resident)
            }
            is PaymentsIntent.ClearResidentSelection -> {
                clearResidentSelection()
            }
            is PaymentsIntent.TogglePaymentSelection -> {
                togglePaymentSelection(intent.payment)
            }
            is PaymentsIntent.UpdatePaymentAmount -> {
                updatePaymentAmount(intent.paymentId, intent.newAmount)
            }
            is PaymentsIntent.RegisterPayments -> {
                registerPayments(intent.context)
            }
        }
    }


    fun loadAllPayments() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val payments = paymentRepository.getAllPayments()

                _state.update {
                    it.copy(
                        allPayments = payments,
                        isLoading = false,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error cargando pagos"
                    )
                }
            }
        }
    }


    // Cargar residentes
    fun loadResidents() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val residents = userRepository.getResidents()
                _state.update {
                    it.copy(
                        residents = residents,
                        isLoading = false
                    )
                }
                Log.d("PaymentsViewModel", "Residentes cargados: ${residents.size}")
            } catch (e: Exception) {
                Log.e("PaymentsViewModel", "Error loading residents: $e")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar residentes"
                    )
                }
            }
        }
    }

    private fun selectResident(resident: User) {
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        selectedResident = resident,
                        selectedPayments = emptyMap(), // Limpiar selecciones anteriores
                        pendingPayments = emptyList()
                    )
                }
                Log.d("PaymentsViewModel", "Residente seleccionado: ${resident.name}, ID: ${resident.id}")

                // Cargar los pagos pendientes del residente
                loadPendingPayments(resident.id)

            } catch (e: Exception) {
                Log.e("PaymentsViewModel", "Error selecting resident: $e")
                _state.update { it.copy(errorMessage = "Error al seleccionar residente") }
            }
        }
    }

    // Cargar pagos pendientes del residente
    private fun loadPendingPayments(residentId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val allPayments = paymentRepository.getPaymentsByUser(residentId)
                Log.d("PaymentsViewModel", "Todos los pagos cargados: ${allPayments.size}")

                // Filtrar solo los que están "pending" o "partial"
                val pendingPayments = allPayments.filter {
                    it.status == "Pendiente" || it.status == "Parcial"
                }

                _state.update {
                    it.copy(
                        pendingPayments = pendingPayments,
                        isLoading = false
                    )
                }

                Log.d("PaymentsViewModel", "Pagos pendientes cargados: ${pendingPayments.size}")

            } catch (e: Exception) {
                Log.e("PaymentsViewModel", "Error loading pending payments: $e")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar pagos pendientes"
                    )
                }
            }
        }
    }


    private fun togglePaymentSelection(payment: Payment) {
        val paymentId = payment.id ?: return

        _state.update { currentState ->
            val currentSelections = currentState.selectedPayments.toMutableMap()

            if (currentSelections.containsKey(paymentId)) {
                Log.d("PAYMENTBUG", "togglePaymentSelection → DESELECCIONADO paymentId=$paymentId")
                currentSelections.remove(paymentId)
            } else {
                val montoPendienteCuota = (payment.amount - payment.paidAmount).coerceAtLeast(0f)
                val totalMultas = payment.fines.sumOf { it.amount.toDouble() }.toFloat()
                val totalPendienteVisual = montoPendienteCuota + totalMultas

                Log.d(
                    "PAYMENTBUG",
                    """
                togglePaymentSelection → SELECCIONADO
                paymentId=$paymentId
                amountBase=${payment.amount}
                paidAmount=${payment.paidAmount}
                montoPendienteCuota=$montoPendienteCuota
                totalMultas=$totalMultas
                totalPendienteVisual=$totalPendienteVisual
                """.trimIndent()
                )

                val selectedPayment = SelectedPayment(
                    paymentId = paymentId,
                    mes = payment.month,
                    year = payment.year,
                    montoTotal = payment.amount,
                    montoPendiente = montoPendienteCuota,
                    totalMultas = totalMultas,
                    montoPagar = totalPendienteVisual,
                    isPagoCompleto = false
                )

                currentSelections[paymentId] = selectedPayment
            }

            currentState.copy(selectedPayments = currentSelections)
        }
    }



    private fun updatePaymentAmount(paymentId: Int, newAmount: Float) {
        _state.update { currentState ->
            val selections = currentState.selectedPayments.toMutableMap()
            val sp = selections[paymentId] ?: return@update currentState

            val montoIngresado = newAmount.coerceAtLeast(0f)

            // total real que debe (cuota + multas)
            val totalDebe = sp.montoPendiente + sp.totalMultas

            val isPagoCompleto = montoIngresado >= totalDebe && totalDebe > 0f

            val updated = sp.copy(
                montoPagar = montoIngresado,
                isPagoCompleto = isPagoCompleto
            )

            selections[paymentId] = updated

            Log.d(
                "PAYMENTBUG",
                "updatePaymentAmount FINAL → paymentId=$paymentId " +
                        "montoIngresado=$montoIngresado totalDebe=$totalDebe isPagoCompleto=$isPagoCompleto"
            )

            currentState.copy(selectedPayments = selections)
        }
    }




    private fun registerPayments(context: Context) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val selected = _state.value.selectedPayments.values.toList()
                if (selected.isEmpty()) {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "No hay pagos seleccionados")
                    }
                    return@launch
                }

                selected.forEach { sp ->
                    Log.d(
                        "PAYMENTBUG",
                        """
                    registerPayments → ENVIANDO A REPO
                    paymentId=${sp.paymentId}
                    mes=${sp.mes}/${sp.year}
                    montoTotalBase=${sp.montoTotal}
                    montoPendienteCuota=${sp.montoPendiente}
                    montoPagar=${sp.montoPagar}
                    isPagoCompleto=${sp.isPagoCompleto}
                    """.trimIndent()
                    )
                }

                // 1️⃣ Registrar pagos
                val transactionIds = paymentRepository.registerPayment(selected)
                if (transactionIds.isEmpty()) throw Exception("No se registraron transacciones")

                // 2️⃣ Construir factura
                val invoice = paymentRepository.buildInvoiceFromTransactions(transactionIds)

                // 3️⃣ Generar PDF + subir a Supabase
                val invoiceUrl = paymentRepository.generateAndUploadInvoice(
                    context = context,
                    invoice = invoice
                )

                // 4️⃣ Guardar URL en las transacciones
                paymentRepository.updateInvoiceUrlForTransactions(
                    transactionIds = transactionIds,
                    invoiceUrl = invoiceUrl
                )

                // 5️⃣ Éxito
                _state.update {
                    it.copy(
                        isLoading = false,
                        success = PaymentSuccess.InvoiceGenerated(
                            invoice.copy(invoiceUrl = invoiceUrl)
                        ),
                        selectedPayments = emptyMap()
                    )
                }

                _state.value.selectedResident?.let { resident ->
                    loadPendingPayments(resident.id)
                }

            } catch (e: Exception) {
                Log.e("PaymentsVM", "🔥 ERROR en registerPayments", e)
                _state.update {
                    it.copy(isLoading = false, errorMessage = "Error al registrar pagos")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun downloadInvoiceFromSupabase(
        context: Context,
        fileUrl: String,
        fileName: String
    ): Uri? {
        return paymentRepository.downloadInvoiceFromSupabase(
            context = context,
            fileUrl = fileUrl,
            fileName = fileName
        )
    }



    private fun clearResidentSelection() {
        _state.update {
            it.copy(
                selectedResident = null,
                pendingPayments = emptyList(),
                selectedPayments = emptyMap()
            )
        }
        Log.d("PaymentsViewModel", "Selección de residente limpiada")
    }

    fun clearSuccess() {
        _state.update { it.copy(success = null) }
    }







}