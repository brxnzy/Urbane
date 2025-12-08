package com.example.urbane.ui.admin.payments.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import androidx.compose.ui.Alignment
import com.example.urbane.data.model.Payment
import com.example.urbane.data.model.PaymentTransaction
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
                2 -> PaymentHistoryTab(viewModel)
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
fun PaymentHistoryTab(viewModel: PaymentsViewModel) {
    val state by viewModel.state.collectAsState()
    var expandedPaymentId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllPayments()
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
            items(state.allPayments) { pago ->
                PaymentHistoryCard(
                    pago = pago,
                    isExpanded = expandedPaymentId == pago.id,
                    onExpandToggle = {
                        expandedPaymentId = if (expandedPaymentId == pago.id) null else pago.id
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(
    pago: Payment,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(intToMonth(pago.month) + " " + pago.year)
            // Header con nombre y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pago.resident?.name?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusBadge(status = pago.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información principal del pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Monto Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$${String.format("%.2f", pago.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        "Pagado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$${String.format("%.2f", pago.paidAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pago.paidAmount > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        "Saldo Pendiente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val pendiente = pago.amount - pago.paidAmount
                    Text(
                        "$${String.format("%.2f", pendiente)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pendiente > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Botón para expandir si hay transacciones y el pago es parcial
            if (pago.status.equals("Parcial", ignoreCase = true) && pago.paymentTransactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandToggle() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ver transacciones (${pago.paymentTransactions.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Lista de transacciones expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        "Transacciones",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    pago.paymentTransactions.forEach { transaction ->
                        TransactionItem(transaction = transaction)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val estadoColor = when(status.lowercase()) {
        "pagado" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "parcial" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "pendiente" -> Color(0xFFFFF9C4) to Color(0xFFE65100)
        "moroso" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color.LightGray to Color.DarkGray
    }

    Surface(
        color = estadoColor.first,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = estadoColor.second,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionItem(transaction: PaymentTransaction) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del método de pago
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = when(transaction.method.lowercase()) {
                        "cash", "efectivo" -> Icons.Default.AttachMoney
                        "transfer", "transferencia" -> Icons.Default.AccountBalance
                        "card", "tarjeta" -> Icons.Default.CreditCard
                        else -> Icons.Default.Payment
                    },
                    contentDescription = transaction.method,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la transacción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    getMethodDisplayName(transaction.method),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                transaction.createdAt?.let {
                    Text(
                        it,

                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }



                        }
                    }
                }


            // Monto
            Text(
                "$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
    }



// Función helper para formatear la fecha
fun formatTransactionDate(dateString: String?): String? {
    return try {
        // Asumiendo formato ISO 8601: "2025-12-05T14:20:10.000Z"
        val date = dateString?.substring(0, 10) // "2025-12-05"
        val parts = date?.split("-")
        val months = listOf(
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        )
        "${parts?.get(2)} ${months[parts?.get(1)?.toInt()?.minus(1) ?: 1]} ${parts?.get(0)}"
    } catch (e: Exception) {
        dateString
    }
}

// Función helper para obtener el nombre del método de pago
fun getMethodDisplayName(method: String): String {
    return when(method.lowercase()) {
        "cash" -> "Efectivo"
        "transfer" -> "Transferencia"
        "card" -> "Tarjeta"
        else -> method.replaceFirstChar { it.uppercase() }
    }
}
