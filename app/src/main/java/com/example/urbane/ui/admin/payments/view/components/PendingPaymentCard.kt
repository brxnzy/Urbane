package com.example.urbane.ui.admin.payments.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.urbane.data.model.Payment
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import com.example.urbane.utils.intToMonth


@Composable
fun PendingPaymentCard(
    payment: Payment,
    isSelected: Boolean,
    selectedPayment: SelectedPayment?,
    onToggleSelection: () -> Unit,
    onAmountChange: (Float) -> Unit
) {

    // ✅ Multas
    val totalMultas = payment.fines.sumOf { it.amount.toDouble() }.toFloat()

    // ✅ Total original (cuota base + multas)
    val totalOriginal = payment.amount + totalMultas  // 1000

    // ✅ Lo que REALMENTE debe (total - ya pagado)
    val pendienteReal = (totalOriginal - payment.paidAmount).coerceAtLeast(0f)  // 1000 - 700 = 300

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {

                Text(
                    text = intToMonth(payment.month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Pendiente total: RD$ %.2f".format(pendienteReal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )

                if (payment.paidAmount > 0f) {
                    Text(
                        text = "Pagado: RD$ %.2f".format(payment.paidAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (payment.fines.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Extras",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )

                    payment.fines.forEach { fine ->
                        Text(
                            text = "• ${fine.title}: RD$ %.2f".format(fine.amount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = "RD$ %.2f".format(pendienteReal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isSelected && selectedPayment != null) {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(top = 0.dp)
            ) {

                var amountText by remember(pendienteReal) {
                    mutableStateOf(pendienteReal.toString())
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // ✅ Permitir texto vacío temporalmente (mientras escribe)
                        if (newValue.isEmpty()) {
                            amountText = ""
                            onAmountChange(0f)
                            return@OutlinedTextField
                        }

                        // ✅ Validar que solo sean números y punto decimal
                        val regex = Regex("^\\d*\\.?\\d*$")
                        if (!regex.matches(newValue)) {
                            return@OutlinedTextField  // Rechazar caracteres inválidos
                        }

                        val enteredAmount = newValue.toFloatOrNull() ?: 0f

                        // ✅ LIMITAR: No permitir más del pendiente real
                        if (enteredAmount <= pendienteReal) {
                            amountText = newValue
                            onAmountChange(enteredAmount)
                        } else {
                            // ✅ Si intenta poner más, lo limita al máximo
                            amountText = pendienteReal.toString()
                            onAmountChange(pendienteReal)
                        }
                    },
                    label = { Text("Monto a pagar") },
                    prefix = { Text("RD$ ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            "Máximo: RD$ %.2f".format(pendienteReal),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = (amountText.toFloatOrNull() ?: 0f) > pendienteReal
                )

                Spacer(Modifier.height(8.dp))

                val esPagoCompleto = selectedPayment.montoPagar >= pendienteReal

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = if (esPagoCompleto)
                            "Pago completo"
                        else
                            "Pago parcial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        imageVector = if (esPagoCompleto)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (esPagoCompleto)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}