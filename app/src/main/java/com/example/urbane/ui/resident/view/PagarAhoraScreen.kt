package com.example.urbane.ui.resident.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagarAhoraScreen(navController: NavController, concepto: String, monto: Double, fecha: String) {
    var selectedMetodo by remember { mutableStateOf("Tarjeta de Crédito") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var nombreTitular by remember { mutableStateOf("") }
    var fechaExpiracion by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Detectar si es pago de alquiler
    val esAlquiler = concepto.contains("Alquiler", ignoreCase = true)

    val metodosPago = if (esAlquiler) {
        listOf("Transferencia Bancaria", "Tarjeta de Crédito", "Tarjeta de Débito")
    } else {
        listOf("Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia Bancaria")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esAlquiler) "Pagar Alquiler" else "Realizar Pago",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Resumen del Pago con gradiente
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (esAlquiler) {
                                        listOf(
                                            Color(0xFF6200EE),
                                            Color(0xFF3700B3)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (esAlquiler) Icons.Default.Home else Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    "Resumen del Pago",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Concepto:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    concepto,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            if (esAlquiler) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Beneficiario:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        "Juan Pérez",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Fecha:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    fecha,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Total a Pagar",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Alerta especial para alquiler
            if (esAlquiler) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6200EE).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF6200EE).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF6200EE),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Este pago será enviado directamente al propietario de la unidad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Método de Pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        metodosPago.forEach { metodo ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMetodo = metodo },
                                color = if (selectedMetodo == metodo) {
                                    if (esAlquiler)
                                        Color(0xFF6200EE).copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedMetodo == metodo,
                                        onClick = { selectedMetodo = metodo },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        color = if (selectedMetodo == metodo) {
                                            if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                                        } else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            when (metodo) {
                                                "Tarjeta de Crédito" -> Icons.Default.CreditCard
                                                "Tarjeta de Débito" -> Icons.Default.Payment
                                                else -> Icons.Default.AccountBalance
                                            },
                                            contentDescription = null,
                                            tint = if (selectedMetodo == metodo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        metodo,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (selectedMetodo == metodo) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                            if (metodo != metodosPago.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            if (selectedMetodo != "Transferencia Bancaria") {
                item {
                    Text(
                        "Información de Pago",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = numeroTarjeta,
                        onValueChange = { if (it.length <= 16) numeroTarjeta = it.filter { char -> char.isDigit() } },
                        label = { Text("Número de Tarjeta") },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, contentDescription = null)
                        },
                        placeholder = { Text("1234 5678 9012 3456") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = nombreTitular,
                        onValueChange = { nombreTitular = it },
                        label = { Text("Nombre del Titular") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        placeholder = { Text("NOMBRE APELLIDO") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fechaExpiracion,
                            onValueChange = {
                                if (it.length <= 5) {
                                    fechaExpiracion = it.filter { char -> char.isDigit() || char == '/' }
                                }
                            },
                            label = { Text("Fecha Exp.") },
                            placeholder = { Text("MM/YY") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                                focusedLabelColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it.filter { char -> char.isDigit() } },
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                                focusedLabelColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esAlquiler)
                                Color(0xFF6200EE).copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (esAlquiler)
                                        Color(0xFF6200EE).copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    if (esAlquiler)
                                        "Datos del Propietario"
                                    else
                                        "Información Bancaria",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (esAlquiler) {
                                InfoRow("Beneficiario", "Juan Pérez García")
                                InfoRow("Banco", "Banco BHD León")
                                InfoRow("Cuenta", "9876-5432-10-1")
                                InfoRow("Tipo", "Cuenta de Ahorros")
                            } else {
                                InfoRow("Banco", "Banco Popular Dominicano")
                                InfoRow("Cuenta", "1234-5678-90-1")
                                InfoRow("Tipo", "Cuenta Corriente")
                                InfoRow("Beneficiario", "Condominio Urbane")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Button(
                    onClick = { showSuccessDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                    ),
                    enabled = if (selectedMetodo != "Transferencia Bancaria") {
                        numeroTarjeta.isNotEmpty() && nombreTitular.isNotEmpty() &&
                                fechaExpiracion.isNotEmpty() && cvv.isNotEmpty()
                    } else true
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (esAlquiler) "Pagar al Propietario" else "Procesar Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp
                    )
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp)
                    )
                }
            },
            title = {
                Text(
                    if (esAlquiler) "¡Pago Exitoso!" else "¡Pago Procesado!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        if (esAlquiler)
                            "Tu pago de alquiler ha sido procesado correctamente y se ha enviado al propietario."
                        else
                            "Tu pago ha sido procesado correctamente.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Monto:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Método:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    selectedMetodo,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar", modifier = Modifier.padding(vertical = 4.dp))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}