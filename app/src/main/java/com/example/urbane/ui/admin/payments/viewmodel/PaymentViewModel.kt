package com.example.urbane.ui.admin.payments.viewmodel

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.InvoiceData
import com.example.urbane.data.repository.PaymentRepository
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.data.model.User
import com.example.urbane.data.model.Payment
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.admin.payments.model.*
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


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
                registerPayments()
            }
        }
    }

    fun generateInvoicePdf(context: Context, invoice: InvoiceData) {
        viewModelScope.launch {
            try {
            paymentRepository.generateAndUploadInvoice(context, invoice)
            }catch (e: Exception){
                Log.e("PaymentsViewModel", "Error generateInvoicePdf: $e")
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

    // Seleccionar un residente
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

    // Toggle de selección de un pago (checkbox)
    private fun togglePaymentSelection(payment: Payment) {
        val paymentId = payment.id ?: return

        _state.update { currentState ->
            val currentSelections = currentState.selectedPayments.toMutableMap()

            if (currentSelections.containsKey(paymentId)) {
                // Si ya está seleccionado, lo deseleccionamos
                currentSelections.remove(paymentId)
                Log.d("PaymentsViewModel", "Pago deseleccionado: $paymentId")
            } else {
                // Si no está seleccionado, lo agregamos
                val montoPendiente = payment.amount - payment.paidAmount

                val selectedPayment = SelectedPayment(
                    paymentId = paymentId,
                    mes = payment.month,
                    year = payment.year,
                    montoTotal = payment.amount,
                    montoPendiente = montoPendiente,
                    montoPagar = montoPendiente, // Por defecto, pagar todo lo pendiente
                    isPagoCompleto = true // Por defecto, pago completo
                )

                currentSelections[paymentId] = selectedPayment
                Log.d("PaymentsViewModel", "Pago seleccionado: $paymentId, monto pendiente: $montoPendiente")
            }

            currentState.copy(selectedPayments = currentSelections)
        }
    }

    private fun updatePaymentAmount(paymentId: Int, newAmount: Float) {
        _state.update { currentState ->
            val currentSelections = currentState.selectedPayments.toMutableMap()
            val selectedPayment = currentSelections[paymentId] ?: return@update currentState

            // Validar que el nuevo monto no sea mayor al pendiente ni negativo
            val validAmount = when {
                newAmount < 0 -> 0f
                newAmount > selectedPayment.montoPendiente -> selectedPayment.montoPendiente
                else -> newAmount
            }

            // Determinar si es pago completo o parcial
            val isPagoCompleto = validAmount >= selectedPayment.montoPendiente

            val updatedPayment = selectedPayment.copy(
                montoPagar = validAmount,
                isPagoCompleto = isPagoCompleto
            )

            currentSelections[paymentId] = updatedPayment

            Log.d("PaymentsViewModel", "Monto actualizado para pago $paymentId: $validAmount (completo: $isPagoCompleto)")

            currentState.copy(selectedPayments = currentSelections)
        }
    }


    private fun registerPayments() {
        viewModelScope.launch {
            try {

                _state.update { it.copy(isLoading = true) }

                val selected = _state.value.selectedPayments.values.toList()

                if (selected.isEmpty()) {
                    Log.e("PaymentsVM", "❌ Lista de pagos vacía")

                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No hay pagos seleccionados"
                        )
                    }

                    return@launch
                }

                val transactionIds =
                    paymentRepository.registerPayment(selected)


                if (transactionIds.isEmpty()) {
                    Log.e("PaymentsVM", "❌ registerPayment devolvió lista VACÍA")
                    throw Exception("registerPayment devolvió lista vacía")
                }


                val invoice =
                    paymentRepository.buildInvoiceFromTransactions(transactionIds)

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = PaymentSuccess.InvoiceGenerated(
                            invoice = invoice
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
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al registrar los pagos $e"
                    )
                }
            }
        }
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