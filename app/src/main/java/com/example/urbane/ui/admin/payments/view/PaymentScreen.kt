package com.example.urbane.ui.admin.payments.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import com.example.urbane.data.model.Payment
import com.example.urbane.ui.admin.payments.model.PaymentsIntent
import com.example.urbane.ui.admin.payments.model.SelectedPayment
import com.example.urbane.utils.intToMonth
data class TransferRequest(
    val id: Int,
    val residente: String,
    val monto: Double,
    val fecha: String,
    val comprobante: String
)

data class PaymentHistory(
    val id: Int,
    val residente: String,
    val monto: Double,
    val metodo: String,
    val fecha: String,
    val estado: String,
    val saldoPendiente: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(viewModel: PaymentsViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Registrar Pago", "Solicitudes (3)", "Historial")

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadResidents()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Content
            when (selectedTab) {
                0 -> RegisterPaymentScreen(viewModel)
                1 -> TransferRequestsTab()
                2 -> PaymentHistoryTab()
            }
        }
    }
}
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





@Composable
fun TransferRequestsTab() {
    val solicitudes = remember {
        listOf(
            TransferRequest(1, "Carlos Méndez", 2500.0, "2024-12-01", "IMG_001.jpg"),
            TransferRequest(2, "Ana López", 2500.0, "2024-12-02", "IMG_002.jpg"),
            TransferRequest(3, "Pedro Ramírez", 1500.0, "2024-12-01", "IMG_003.jpg")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Solicitudes de Transferencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${solicitudes.size} pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(solicitudes) { solicitud ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            solicitud.residente,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Pendiente",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text(
                                "Monto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$${String.format("%,.2f", solicitud.monto)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                "Fecha",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                solicitud.fecha,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                "Comprobante",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                solicitud.comprobante,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* Aprobar */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aprobar")
                        }

                        OutlinedButton(
                            onClick = { /* Rechazar */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rechazar")
                        }

                        IconButton(
                            onClick = { /* Ver comprobante */ }
                        ) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = "Ver")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PaymentHistoryTab() {
    val historial = remember {
        listOf(
            PaymentHistory(1, "María García", 2500.0, "Efectivo", "2024-11-28", "pagado", 0.0),
            PaymentHistory(2, "Juan Pérez", 1000.0, "Transferencia", "2024-11-25", "abono", 1500.0),
            PaymentHistory(3, "Luis Fernández", 2500.0, "Efectivo", "2024-11-20", "pagado", 0.0),
            PaymentHistory(4, "Carmen Silva", 0.0, "-", "-", "pendiente", 2500.0),
            PaymentHistory(5, "Roberto Castro", 0.0, "-", "2024-11-30", "moroso", 2500.0)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Historial de Pagos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { /* Filtrar */ }) {
                    Icon(Icons.Default.FilterList, "Filtrar")
                }
                IconButton(onClick = { /* Buscar */ }) {
                    Icon(Icons.Default.Search, "Buscar")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historial) { pago ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                pago.residente,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            val estadoColor = when(pago.estado) {
                                "pagado" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                "pendiente" -> Color(0xFFFFF9C4) to Color(0xFFE65100)
                                "moroso" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                                "abono" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                                else -> Color.LightGray to Color.DarkGray
                            }

                            Surface(
                                color = estadoColor.first,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    pago.estado.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = estadoColor.second,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Monto",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (pago.monto > 0) "$${String.format("%.2f", pago.monto)}" else "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    "Método",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    pago.metodo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    "Saldo Pendiente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (pago.saldoPendiente > 0)
                                        "$${String.format("%.2f", pago.saldoPendiente)}"
                                    else "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pago.saldoPendiente > 0) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                                )
                            }
                        }

                        if (pago.fecha != "-") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Fecha: ${pago.fecha}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}