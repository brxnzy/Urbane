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
    val montoPendiente = payment.amount - payment.paidAmount

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
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )

            // Información principal
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = intToMonth(payment.month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Pendiente: RD$ %.2f".format(montoPendiente),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )

                if (payment.paidAmount > 0) {
                    Text(
                        text = "Pagado: RD$ %.2f".format(payment.paidAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Monto total (a la derecha)
            Text(
                text = "RD$ %.2f".format(payment.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Input de monto (expandible cuando está seleccionado)
        if (isSelected && selectedPayment != null) {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Column(modifier = Modifier.padding(16.dp).padding(top = 0.dp)) {
                var amountText by remember(selectedPayment.montoPagar) {
                    mutableStateOf(selectedPayment.montoPagar.toString())
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue
                        newValue.toFloatOrNull()?.let { amount ->
                            onAmountChange(amount)
                        }
                    },
                    label = { Text("Monto a pagar") },
                    prefix = { Text("RD$ ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Indicador de tipo de pago
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedPayment.isPagoCompleto) "Pago completo" else "Pago parcial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        imageVector = if (selectedPayment.isPagoCompleto)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (selectedPayment.isPagoCompleto)
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
