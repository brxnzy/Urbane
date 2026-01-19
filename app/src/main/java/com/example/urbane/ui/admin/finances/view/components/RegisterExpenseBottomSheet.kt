package com.example.urbane.ui.admin.finances.view.components
import FinancesViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.finances.model.FinancesIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterExpenseBottomSheet(
    viewModel: FinancesViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Registrar Egreso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Balance disponible: $${"%.2f".format(state.balance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.balance > 0) Color(0xFF059669) else Color(0xFFDC2626),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Monto
            OutlinedTextField(
                value = state.amount,
                onValueChange = { viewModel.handleIntent(FinancesIntent.UpdateAmount(it)) },
                label = { Text("Monto") },
                placeholder = { Text("0.00") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = state.amount.toDoubleOrNull()?.let { it > state.balance } == true
            )

            // Mensaje de advertencia si excede balance
            if (state.amount.toDoubleOrNull()?.let { it > state.balance } == true) {
                Text(
                    text = "⚠️ El monto excede el balance disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Descripción
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.handleIntent(FinancesIntent.UpdateDescription(it)) },
                label = { Text("Descripción") },
                placeholder = { Text("Ej: Evento navideño") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Registrar
            Button(
                onClick = {
                    viewModel.handleIntent(FinancesIntent.RegisterExpense)
                    onDismiss()
                          },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading &&
                        state.amount.toDoubleOrNull()?.let { it > 0 && it <= state.balance } == true &&
                        state.description.isNotBlank()
            ) {
                Text(if (state.isLoading) "Registrando..." else "Registrar Egreso")
            }

            // Mensaje de error
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}