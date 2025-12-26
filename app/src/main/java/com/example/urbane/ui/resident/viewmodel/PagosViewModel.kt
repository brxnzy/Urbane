package com.example.urbane.ui.resident.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
class PagosViewModel(

) : ViewModel() {

    private val _uiState = MutableStateFlow<PagosUiState>(PagosUiState.Loading)
    val uiState: StateFlow<PagosUiState> = _uiState.asStateFlow()

    private val supabaseClient = supabase

    private val _expandedPayments = MutableStateFlow<Set<Long>>(emptySet())
    val expandedPayments: StateFlow<Set<Long>> = _expandedPayments.asStateFlow()

        fun loadPagos(residentId: String) {
            viewModelScope.launch {
                try {
                    _uiState.value = PagosUiState.Loading

                    val pagosResponse = supabaseClient.from("payments")
                        .select{
                            filter{

                        eq("residentId", residentId)
                            }
                        }
                        .decodeList<PaymentDto>()

                    Log.d("PagosViewmodel","$pagosResponse")

                    // Filtrar solo pendientes y parciales
                    val pagosPendientes = pagosResponse.filter {
                        it.status == "Pendiente" || it.status == "Parcial"
                    }

                    // Cargar transacciones para pagos parciales y pagados
                    val pagosConTransacciones = pagosPendientes.map { pagoDto ->
                        val transacciones = if (pagoDto.status == "Parcial" || pagoDto.status == "Pagado") {
                            getPaymentTransactions(pagoDto.id)
                        } else {
                            emptyList()
                        }
                        pagoDto.toPayment(transacciones)
                    }

                _uiState.value = PagosUiState.Success(
                    pagosPendientes = pagosConTransacciones
                )
            } catch (e: Exception) {
                _uiState.value = PagosUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private suspend fun getPaymentTransactions(paymentId: Long): List<PaymentTransaction> {
        return try {
            val transactionsResponse = supabaseClient.from("payments_transactions")
                .select(){
                    filter{
                        eq("paymentId", paymentId)
                    }
                }
                .decodeList<PaymentTransactionDto>()

            transactionsResponse.map { it.toPaymentTransaction() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun togglePaymentExpansion(paymentId: Long) {
        val currentExpanded = _expandedPayments.value.toMutableSet()
        if (currentExpanded.contains(paymentId)) {
            currentExpanded.remove(paymentId)
        } else {
            currentExpanded.add(paymentId)
        }
        _expandedPayments.value = currentExpanded
    }
}

// DTOs para Supabase
@Serializable
data class PaymentDto(
    val id: Long,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("residentId") val residentId: String,
    val amount: Double,
    @SerialName("paidAmount") val paidAmount: Double = 0.0,
    val status: String,
    val month: Int,
    val year: Int,
    @SerialName("residentialId") val residentialId: Long? = null,
    @SerialName("updatedAt") val updatedAt: String? = null
) {
    fun toPayment(transacciones: List<PaymentTransaction> = emptyList()) = Payment(
        id = id,
        createdAt = createdAt,
        residentId = residentId,
        amount = amount,
        paidAmount = paidAmount,
        status = PaymentStatus.valueOf(status),
        month = month,
        year = year,
        residentialId = residentialId,
        updatedAt = updatedAt,
        transacciones = transacciones
    )
}

@Serializable
data class PaymentTransactionDto(
    val id: Long,

    @SerialName("createdAt") val createdAt: String,
    @SerialName("paymentId") val paymentId: Long,
    val amount: Double,
    val method: String,
    @SerialName("residentialId") val residentialId: Long,
    @SerialName("invoiceUrl") val invoiceUrl: String? = null
) {
    fun toPaymentTransaction() = PaymentTransaction(
        id = id,
        createdAt = createdAt,
        paymentId = paymentId,
        amount = amount,
        method = PaymentMethod.valueOf(method),
        residentialId = residentialId,
        invoiceUrl = invoiceUrl
    )
}

// UI State
sealed class PagosUiState {
    object Loading : PagosUiState()
    data class Success(
        val pagosPendientes: List<Payment>? = null
    ) : PagosUiState()
    data class Error(val message: String) : PagosUiState()
}

// Domain Models
data class Payment(
    val id: Long,
    val createdAt: String,
    val residentId: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val status: PaymentStatus,
    val month: Int,
    val year: Int,
    val residentialId: Long?,
    val updatedAt: String?,
    val transacciones: List<PaymentTransaction> = emptyList()
) {
    val pendingAmount: Double
        get() = amount - paidAmount

    val monthName: String
        get() = when(month) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> "Mes $month"
        }

    val displayDate: String
        get() = "$monthName $year"
}

data class PaymentTransaction(
    val id: Long,
    val createdAt: String,
    val paymentId: Long,
    val amount: Double,
    val method: PaymentMethod,
    val residentialId: Long,
    val invoiceUrl: String?
) {
    val displayDate: String
        get() {
            return try {
                val parts = createdAt.split("T")[0].split("-")
                val year = parts[0]
                val month = parts[1].toInt()
                val day = parts[2]
                val monthName = when(month) {
                    1 -> "Ene"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Abr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Ago"
                    9 -> "Sep"
                    10 -> "Oct"
                    11 -> "Nov"
                    12 -> "Dic"
                    else -> month.toString()
                }
                "$day $monthName $year"
            } catch (e: Exception) {
                createdAt
            }
        }
}

enum class PaymentStatus {
    Pendiente,
    Parcial,
    Pagado,
    Vencido
}

enum class PaymentMethod {
    Transferencia,
    Tarjeta,
    Efectivo,
    Cheque
}