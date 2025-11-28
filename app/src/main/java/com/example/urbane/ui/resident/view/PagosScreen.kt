package com.example.urbane.ui.resident.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PagosScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "Historial", "Comprobantes")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> PagosPendientesContent(navController)
            1 -> PagosHistorialContent()
            2 -> ComprobantesContent()
        }
    }
}

@Composable
fun PagosPendientesContent(navController: NavController) {
    val pagosPendientesList = remember {
        listOf(
            Triple("Cuota de Mantenimiento - Noviembre", "Vence: 10 Nov 2025", false),
            Triple("Cuota de Mantenimiento - Octubre", "Vence: 10 Oct 2025", true),
            Triple("Cuota de Mantenimiento - Septiembre", "Vence: 10 Sep 2025", true)
        )
    }

    val cantidadPendientes = pagosPendientesList.count { !it.third }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (cantidadPendientes > 0)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (cantidadPendientes > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (cantidadPendientes > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "$cantidadPendientes pago${if (cantidadPendientes != 1) "s" else ""} pendiente${if (cantidadPendientes != 1) "s" else ""}",
                        fontWeight = FontWeight.Bold,
                        color = if (cantidadPendientes > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(pagosPendientesList) { (concepto, fecha, pagado) ->
            PagoCard(
                concepto = concepto,
                monto = 2500.00,
                fecha = fecha,
                pagado = pagado,
                navController = navController
            )
        }
    }
}

@Composable
fun PagosHistorialContent() {
    val historialList = remember {
        List(5) { index ->
            mapOf(
                "concepto" to "Cuota de Mantenimiento",
                "monto" to 2500.00,
                "fecha" to "15 Oct 2025",
                "metodo" to "Transferencia"
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(historialList) { pago ->
            PagoHistorialCard(
                concepto = pago["concepto"] as String,
                monto = pago["monto"] as Double,
                fecha = pago["fecha"] as String,
                metodo = pago["metodo"] as String
            )
        }
    }
}

@Composable
fun ComprobantesContent() {
    val comprobantesList = remember {
        listOf(
            Triple("Cuota de Mantenimiento - Octubre 2025", "15 Oct 2025", 2500.00),
            Triple("Cuota de Mantenimiento - Septiembre 2025", "15 Sep 2025", 2500.00),
            Triple("Cuota de Mantenimiento - Agosto 2025", "15 Ago 2025", 2500.00)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Comprobantes de Pago",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(comprobantesList) { (concepto, fecha, monto) ->
            ComprobanteCard(
                concepto = concepto,
                fecha = fecha,
                monto = monto
            )
        }
    }
}

@Composable
fun ComprobanteCard(concepto: String, fecha: String, monto: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Abrir comprobante */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(concepto, fontWeight = FontWeight.Bold)
                Text(
                    fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PagoCard(concepto: String, monto: Double, fecha: String, pagado: Boolean, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(concepto, fontWeight = FontWeight.Bold)
                    Text(
                        fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pagado) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    )
                }
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!pagado) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val encodedConcepto = URLEncoder.encode(concepto, StandardCharsets.UTF_8.toString())
                        val encodedFecha = URLEncoder.encode(fecha, StandardCharsets.UTF_8.toString())
                        navController.navigate("pagar_ahora/$encodedConcepto/$monto/$encodedFecha")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pagar Ahora")
                }
            }
        }
    }
}

@Composable
fun PagoHistorialCard(concepto: String, monto: Double, fecha: String, metodo: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(concepto, fontWeight = FontWeight.Bold)
                Text(fecha, style = MaterialTheme.typography.bodySmall)
                Text(metodo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Chip1(label = "Pagado")
            }
        }
    }
}

@Composable
fun Chip1(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
    }
}