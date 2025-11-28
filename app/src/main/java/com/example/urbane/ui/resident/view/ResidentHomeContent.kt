package com.example.urbane.ui.resident.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.auth.viewmodel.LoginViewModel

@Composable
fun ResidentHomeContent(sessionManager: SessionManager, loginViewModel: LoginViewModel, navController: NavController) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    when (user) {
                        null -> CircularProgressIndicator()
                        else -> {
                            Text(
                                "Bienvenido/a",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                user.userData?.user?.name ?: "Residente",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Resumen de Cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusCard("Pagos al día", "3/3", Color(0xFF4CAF50))
                        StatusCard("Incidencias", "1", Color(0xFFFFA726))
                    }
                }
            }
        }

        item {
            Text(
                "Accesos Rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.AttachMoney,
                    title = "Realizar Pago",
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.Warning,
                    title = "Nueva Incidencia",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Últimas Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(3) { index ->
            NotificationItem(
                title = when(index) {
                    0 -> "Mantenimiento Programado"
                    1 -> "Recordatorio de Pago"
                    else -> "Nueva Actualización"
                },
                message = "Tap para ver más detalles",
                icon = when(index) {
                    0 -> Icons.Default.Build
                    1 -> Icons.Default.AttachMoney
                    else -> Icons.Default.Info
                }
            )
        }
    }
}

@Composable
fun StatusCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuickAccessCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun NotificationItem(title: String, message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}