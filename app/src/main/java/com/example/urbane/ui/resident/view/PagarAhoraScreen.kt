package com.example.urbane.ui.resident.view

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
                        if (esAlquiler) "Pagar Alquiler" else "Realizar Pago"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (esAlquiler)
                        Color(0xFF6200EE).copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen del Pago
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esAlquiler)
                            Color(0xFF6200EE).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (esAlquiler) Icons.Default.Home else Icons.Default.Payment,
                                contentDescription = null,
                                tint = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Resumen del Pago",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (esAlquiler)
                                    Color(0xFF6200EE)
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Concepto:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                concepto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (esAlquiler) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Beneficiario:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Juan Pérez (Propietario)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6200EE)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Fecha:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                fecha,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total a Pagar:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                            )
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
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Este pago será enviado directamente al propietario de la unidad.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Método de Pago",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        metodosPago.forEach { metodo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMetodo = metodo }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMetodo == metodo,
                                    onClick = { selectedMetodo = metodo }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    when(metodo) {
                                        "Tarjeta de Crédito" -> Icons.Default.CreditCard
                                        "Tarjeta de Débito" -> Icons.Default.Payment
                                        else -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    metodo,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            if (selectedMetodo != "Transferencia Bancaria") {
                item {
                    Text(
                        "Información de Pago",
                        style = MaterialTheme.typography.titleMedium,
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
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
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
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
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
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it.filter { char -> char.isDigit() } },
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
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
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    if (esAlquiler)
                                        "Datos del Propietario para Transferencia"
                                    else
                                        "Información para Transferencia",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (esAlquiler) {
                                Text(
                                    "Beneficiario: Juan Pérez García",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Banco: Banco BHD León",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Cuenta: 9876-5432-10-1",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Tipo: Cuenta de Ahorros",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    "Banco: Banco Popular Dominicano",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Cuenta: 1234-5678-90-1",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Tipo: Cuenta Corriente",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Beneficiario: Condominio Urbane",
                                    style = MaterialTheme.typography.bodyMedium
                                )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esAlquiler) Color(0xFF6200EE) else MaterialTheme.colorScheme.primary
                    ),
                    enabled = if (selectedMetodo != "Transferencia Bancaria") {
                        numeroTarjeta.isNotEmpty() && nombreTitular.isNotEmpty() &&
                                fechaExpiracion.isNotEmpty() && cvv.isNotEmpty()
                    } else true
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (esAlquiler) "Pagar al Propietario" else "Procesar Pago",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", modifier = Modifier.padding(vertical = 8.dp))
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
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
            },
            title = {
                Text(
                    if (esAlquiler) "¡Pago de Alquiler Exitoso!" else "¡Pago Exitoso!"
                )
            },
            text = {
                Column {
                    Text(
                        if (esAlquiler)
                            "Tu pago de alquiler ha sido procesado correctamente."
                        else
                            "Tu pago ha sido procesado correctamente."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Monto: ${NumberFormat.getCurrencyInstance(Locale("es", "DO")).format(monto)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Método: $selectedMetodo")
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
                    )
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}