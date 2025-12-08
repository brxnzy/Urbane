package com.example.urbane.ui.admin.payments.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.payments.model.PaymentsIntent
import com.example.urbane.ui.admin.payments.view.components.PendingPaymentCard
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import kotlin.collections.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(viewModel: PaymentsViewModel) {
    var residentExpanded by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadResidents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // Selección de residente
            Text(
                "RESIDENTE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = residentExpanded,
                onExpandedChange = { residentExpanded = !residentExpanded }
            ) {
                OutlinedTextField(
                    value = state.selectedResident?.name ?: "Seleccione un residente",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(residentExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = residentExpanded,
                    onDismissRequest = { residentExpanded = false }
                ) {
                    state.residents.forEach { resident ->
                        DropdownMenuItem(
                            text = { Text(resident.name) },
                            onClick = {
                                viewModel.handleIntent(
                                    PaymentsIntent.SelectResident(resident)
                                )
                                residentExpanded = false
                            }
                        )
                    }
                }
            }

            if (state.isLoading) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Mostrar pagos pendientes
            if (state.selectedResident != null && !state.isLoading) {
                Spacer(Modifier.height(32.dp))

                Text(
                    "MESES PENDIENTES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))

                state.pendingPayments.forEach { payment ->
                    PendingPaymentCard(
                        payment = payment,
                        isSelected = state.selectedPayments.containsKey(payment.id),
                        selectedPayment = state.selectedPayments[payment.id],
                        onToggleSelection = {
                            viewModel.handleIntent(PaymentsIntent.TogglePaymentSelection(payment))
                        },
                        onAmountChange = { newAmount ->
                            payment.id?.let { id ->
                                viewModel.handleIntent(
                                    PaymentsIntent.UpdatePaymentAmount(id, newAmount)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (state.pendingPayments.isEmpty()) {
                    Text(
                        "Este residente no tiene pagos pendientes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }

                // Botón de registrar pagos (solo si hay pagos seleccionados)
                if (state.selectedPayments.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))

                    val totalAPagar = state.selectedPayments.values.sumOf { it.montoPagar.toDouble() }.toFloat()

                    Text(
                        "TOTAL A PAGAR: RD$ %.2f".format(totalAPagar),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.handleIntent(PaymentsIntent.RegisterPayments)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar Pago en Efectivo")
                    }
                }
            }

            // Mostrar error
            state.errorMessage?.let { error ->
                Spacer(Modifier.height(16.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}