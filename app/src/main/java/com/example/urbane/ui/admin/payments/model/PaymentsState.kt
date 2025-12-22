package com.example.urbane.ui.admin.payments.model

import android.content.Context
import com.example.urbane.data.model.InvoiceData
import com.example.urbane.data.model.User
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.TransactionDetail

data class SelectedPayment(
    val paymentId: Int,           // ID del pago original
    val mes: Int,                 // Mes del pago
    val year: Int,                // Año del pago
    val montoTotal: Float,        // Monto total del pago
    val montoPendiente: Float,    // Monto que aún debe
    val montoPagar: Float,        // Monto que decidió pagar (puede editarse)
    val isPagoCompleto: Boolean   // true si va a pagar todo, false si es abono
)

// State optimizado
data class PaymentsState(
    val isLoading: Boolean = false,
    val residents: List<User> = emptyList(),
    val selectedResident: User? = null,
    val pendingPayments: List<Payment> = emptyList(),
    val selectedPayments: Map<Int, SelectedPayment> = emptyMap(),
    val allPayments: List<Payment> = emptyList(),
    val errorMessage: String? = null,
    val success: PaymentSuccess? = null,
    val transactionDetail: TransactionDetail? = null
)


sealed class PaymentSuccess {
    data class InvoiceGenerated(
        val invoice: InvoiceData
    ) : PaymentSuccess()
}


// Intents completos
sealed class PaymentsIntent {
    data class SelectResident(val resident: User) : PaymentsIntent()
    object ClearResidentSelection : PaymentsIntent()

    // Nuevo: Toggle checkbox de un pago
    data class TogglePaymentSelection(val payment: Payment) : PaymentsIntent()

    // Nuevo: Cambiar el monto a pagar de un pago seleccionado
    data class UpdatePaymentAmount(val paymentId: Int, val newAmount: Float) : PaymentsIntent()

    // Nuevo: Registrar todos los pagos seleccionados
    data class RegisterPayments(val context: Context) : PaymentsIntent()
}


