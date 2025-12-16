package com.example.urbane.ui.resident.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.resident.viewmodel.*
import java.text.NumberFormat
import java.util.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PagosScreen(
    navController: NavController,
    viewModel: PagosViewModel,
    sessionManager: SessionManager
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Pendientes" to Icons.Default.Warning,
        "Historial" to Icons.Default.CheckCircle,
        "Comprobantes" to Icons.Default.Description
    )

    val uiState by viewModel.uiState.collectAsState()
    val expandedPayments by viewModel.expandedPayments.collectAsState()
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value

    LaunchedEffect(user) {
        user?.userId?.let { id ->
            viewModel.loadPagos(id)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Gestión de Pagos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Administra tus pagos y comprobantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, (title, icon) ->
                        EnhancedTab(
                            title = title,
                            icon = icon,
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally() togetherWith
                        fadeOut(animationSpec = tween(300)) + slideOutHorizontally()
            },
            label = ""
        ) { tab ->
            when (tab) {
                0 -> PagosPendientesContent(
                    navController = navController,
                    uiState = uiState,
                    expandedPayments = expandedPayments,
                    onToggleExpand = { paymentId -> viewModel.togglePaymentExpansion(paymentId) }
                )
                1 -> PagosHistorialContent()
                2 -> ComprobantesContent()
            }
        }
    }
}

@Composable
fun EnhancedTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = ""
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = ""
    )

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun PagosPendientesContent(
    navController: NavController,
    uiState: PagosUiState,
    expandedPayments: Set<Long>,
    onToggleExpand: (Long) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    when (uiState) {
        is PagosUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is PagosUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error al cargar pagos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        is PagosUiState.Success -> {
            val pagosPendientes = uiState.pagosPendientes
            val cantidadPendientes = pagosPendientes?.count { it.status == PaymentStatus.Pendiente }
            val totalPendiente = pagosPendientes!!
                ?.filter { it.status == PaymentStatus.Pendiente }
                ?.sumOf { it.pendingAmount }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        if (cantidadPendientes != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (cantidadPendientes > 0)
                                        Color(0xFFE91E63)
                                    else
                                        Color(0xFF4CAF50)
                                )
                            ) {
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .offset(x = (-40).dp, y = (-40).dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    )

                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (cantidadPendientes != null) {
                                                    Icon(
                                                        if (cantidadPendientes > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    if (cantidadPendientes > 0) "Pagos Pendientes" else "¡Todo al día!",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    "$cantidadPendientes concepto${if (cantidadPendientes != 1) "s" else ""}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                        }

                                        if (cantidadPendientes > 0) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Total a pagar:",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                                Text(
                                                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(totalPendiente),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                itemsIndexed(pagosPendientes) { index, pago ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = tween(400, delayMillis = 100 + (index * 100))
                        ) + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        EnhancedPagoCard(
                            pago = pago,
                            navController = navController,
                            isExpanded = expandedPayments.contains(pago.id),
                            onToggleExpand = { onToggleExpand(pago.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedPagoCard(
    pago: Payment,
    navController: NavController,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cuota de Mantenimiento - ${pago.displayDate}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    EnhancedChip(
                        label = when(pago.status) {
                            PaymentStatus.Pendiente -> "Pendiente"
                            PaymentStatus.Parcial -> "Pago Parcial"
                            PaymentStatus.Pagado -> "Pagado"
                            PaymentStatus.Vencido -> "Vencido"
                        },
                        color = when(pago.status) {
                            PaymentStatus.Pendiente -> Color(0xFFFF9800)
                            PaymentStatus.Parcial -> Color(0xFF2196F3)
                            PaymentStatus.Pagado -> Color(0xFF4CAF50)
                            PaymentStatus.Vencido -> Color(0xFFE91E63)
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(pago.amount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (pago.status == PaymentStatus.Parcial) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pagado: ${NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(pago.paidAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Pendiente: ${NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(pago.pendingAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botón para ver transacciones (solo si hay transacciones)
            if (pago.transacciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Ver transacciones (${pago.transacciones.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Lista de transacciones desplegable
            AnimatedVisibility(
                visible = isExpanded && pago.transacciones.isNotEmpty(),
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pago.transacciones.forEach { transaccion ->
                        TransactionItem(transaccion)
                    }
                }
            }

            // Botón de pagar (solo para pendientes y parciales)
            if (pago.status == PaymentStatus.Pendiente || pago.status == PaymentStatus.Parcial) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val montoAPagar = if (pago.status == PaymentStatus.Parcial) {
                            pago.pendingAmount
                        } else {
                            pago.amount
                        }
                        val encodedConcepto = URLEncoder.encode(
                            "Cuota de Mantenimiento - ${pago.displayDate}",
                            StandardCharsets.UTF_8.toString()
                        )
                        navController.navigate("pagar_ahora/$encodedConcepto/$montoAPagar/${pago.id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (pago.status == PaymentStatus.Parcial) "Completar Pago" else "Pagar Ahora",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: PaymentTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when(transaction.method) {
                            PaymentMethod.Transferencia -> Icons.Default.AccountBalance
                            PaymentMethod.Tarjeta -> Icons.Default.CreditCard
                            PaymentMethod.Efectivo -> Icons.Default.Money
                            PaymentMethod.Cheque -> Icons.Default.Description
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        transaction.method.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    transaction.displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                if (transaction.invoiceUrl != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ver recibo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PagosHistorialContent() {
    val historialList = remember {
        List(8) { index ->
            HistorialPago(
                concepto = "Cuota de Mantenimiento",
                monto = 2500.00,
                fecha = "15 ${listOf("Oct", "Sep", "Ago", "Jul", "Jun", "May", "Abr", "Mar")[index]} 2025",
                metodo = listOf("Transferencia", "Tarjeta", "Efectivo")[index % 3],
                referencia = "REF${1000 + index}"
            )
        }
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 30.dp, y = (-30).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )

                        Column(modifier = Modifier.padding(24.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Historial de Pagos",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${historialList.size} pagos realizados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(historialList) { index, pago ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(
                    animationSpec = tween(400, delayMillis = 100 + (index * 80))
                ) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                EnhancedHistorialCard(pago)
            }
        }
    }
}

@Composable
fun EnhancedHistorialCard(pago: HistorialPago) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pago.concepto,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        pago.fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                EnhancedChip(
                    label = pago.metodo,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(pago.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    pago.referencia,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ComprobantesContent() {
    val comprobantesList = remember {
        List(6) { index ->
            ComprobanteData(
                concepto = "Cuota de Mantenimiento - ${listOf("Octubre", "Septiembre", "Agosto", "Julio", "Junio", "Mayo")[index]} 2025",
                fecha = "15 ${listOf("Oct", "Sep", "Ago", "Jul", "Jun", "May")[index]} 2025",
                monto = 2500.00,
                folio = "COMP-${2025}${String.format("%03d", 100 - index)}"
            )
        }
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = (-30).dp, y = 30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )

                        Column(modifier = Modifier.padding(24.dp)) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Comprobantes",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${comprobantesList.size} documentos disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(comprobantesList) { index, comprobante ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(
                    animationSpec = tween(400, delayMillis = 100 + (index * 80))
                ) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                EnhancedComprobanteCard(comprobante)
            }
        }
    }
}

@Composable
fun EnhancedComprobanteCard(comprobante: ComprobanteData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Descargar/Ver comprobante */ }
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    comprobante.concepto,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        comprobante.fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Folio: ${comprobante.folio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(comprobante.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Descargar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// Data classes
data class HistorialPago(
    val concepto: String,
    val monto: Double,
    val fecha: String,
    val metodo: String,
    val referencia: String
)

data class ComprobanteData(
    val concepto: String,
    val fecha: String,
    val monto: Double,
    val folio: String
)