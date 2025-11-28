package com.example.urbane.ui.resident.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.urbane.data.local.SessionManager
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun ResidentScreen(sessionManager: SessionManager, loginViewModel: LoginViewModel, navController: NavController) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(innerNavController) }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { ResidentHomeContent(sessionManager, loginViewModel, navController) }
            composable("pagos") { PagosScreen() }
            composable("incidencias") { IncidenciasScreen() }
            composable("mensajes") { MensajesScreen() }
            composable("perfil") { PerfilScreen(sessionManager, loginViewModel, navController) }
        }
    }
}

// ==================== HOME ====================
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

// ==================== PAGOS ====================
@Composable
fun PagosScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "Historial")

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
            0 -> PagosPendientesContent()
            1 -> PagosHistorialContent()
        }
    }
}

@Composable
fun PagosPendientesContent() {
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
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "0 pagos pendientes",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        items(3) { index ->
            PagoCard(
                concepto = when(index) {
                    0 -> "Cuota de Mantenimiento - Noviembre"
                    1 -> "Cuota de Mantenimiento - Octubre"
                    else -> "Cuota de Mantenimiento - Septiembre"
                },
                monto = 2500.00,
                fecha = when(index) {
                    0 -> "Vence: 10 Nov 2025"
                    1 -> "Vence: 10 Oct 2025"
                    else -> "Vence: 10 Sep 2025"
                },
                pagado = index > 0
            )
        }
    }
}

@Composable
fun PagosHistorialContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { index ->
            PagoHistorialCard(
                concepto = "Cuota de Mantenimiento",
                monto = 2500.00,
                fecha = "15 Oct 2025",
                metodo = "Transferencia"
            )
        }
    }
}

@Composable
fun PagoCard(concepto: String, monto: Double, fecha: String, pagado: Boolean) {
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
                    onClick = { },
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
                Chip(label = "Pagado")
            }
        }
    }
}

// ==================== INCIDENCIAS ====================
@Composable
fun IncidenciasScreen() {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva incidencia")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                IncidenciaCard(
                    titulo = when(index) {
                        0 -> "Fuga de agua en estacionamiento"
                        1 -> "Luz del pasillo fundida"
                        2 -> "Ascensor fuera de servicio"
                        else -> "Ruido excesivo"
                    },
                    descripcion = "Reportado hace ${index + 1} días",
                    estado = when(index) {
                        0 -> "En proceso"
                        1, 2 -> "Pendiente"
                        else -> "Resuelto"
                    }
                )
            }
        }
    }

    if (showDialog) {
        NuevaIncidenciaDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun IncidenciaCard(titulo: String, descripcion: String, estado: String) {
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
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when(estado) {
                            "Resuelto" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "En proceso" -> Color(0xFFFFA726).copy(alpha = 0.2f)
                            else -> Color(0xFFE91E63).copy(alpha = 0.2f)
                        }
                    )
                    .padding(8.dp),
                tint = when(estado) {
                    "Resuelto" -> Color(0xFF4CAF50)
                    "En proceso" -> Color(0xFFFFA726)
                    else -> Color(0xFFE91E63)
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall)
            }
            Chip(label = estado)
        }
    }
}

@Composable
fun NuevaIncidenciaDialog(onDismiss: () -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Incidencia") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Reportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==================== MENSAJES ====================
@Composable
fun MensajesScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { index ->
            MensajeCard(
                remitente = "Administración",
                asunto = when(index) {
                    0 -> "Cambio en horario de recolección"
                    1 -> "Reunión de residentes"
                    2 -> "Actualización de seguridad"
                    3 -> "Mantenimiento programado"
                    else -> "Bienvenida al edificio"
                },
                preview = "Hemos actualizado los horarios...",
                fecha = "${index + 1}d",
                leido = index > 1
            )
        }
    }
}

@Composable
fun MensajeCard(remitente: String, asunto: String, preview: String, fecha: String, leido: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = if (leido)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Message,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        remitente,
                        fontWeight = if (leido) FontWeight.Normal else FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    asunto,
                    fontWeight = if (leido) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

// ==================== PERFIL ====================
@Composable
fun PerfilScreen(sessionManager: SessionManager, loginViewModel: LoginViewModel, navController: NavController) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
        // Aquí puedes guardar la URI en tu ViewModel o SessionManager
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Cabecera del perfil con foto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        // Foto de perfil
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { showImageOptions = true },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { showImageOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.userData?.user?.name?.first()?.toString()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Botón de cámara
                        FloatingActionButton(
                            onClick = { showImageOptions = true },
                            modifier = Modifier.size(36.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        user?.userData?.user?.name ?: "Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        user?.userData?.user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            Text(
                "Información Personal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            PerfilInfoCard(
                icon = Icons.Default.Person,
                label = "Nombre Completo",
                value = user?.userData?.user?.name ?: "No disponible"
            )
        }

        item {
            PerfilInfoCard(
                icon = Icons.Default.Email,
                label = "Correo Electrónico",
                value = user?.userData?.user?.email ?: "No disponible"
            )
        }

        item {
            PerfilInfoCard(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = "+1 (809) 555-1234"
            )
        }

        item {
            PerfilInfoCard(
                icon = Icons.Default.Home,
                label = "Apartamento",
                value = "Torre A - Apt 501"
            )
        }

        item {
            Text(
                "Configuración de Cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            ConfiguracionItem(
                icon = Icons.Default.Edit,
                titulo = "Editar Información Personal",
                onClick = { }
            )
        }

        item {
            ConfiguracionItem(
                icon = Icons.Default.Lock,
                titulo = "Cambiar Contraseña",
                onClick = { }
            )
        }

        item {
            ConfiguracionItem(
                icon = Icons.Default.Notifications,
                titulo = "Preferencias de Notificaciones",
                onClick = { }
            )
        }

        item {
            ConfiguracionItem(
                icon = Icons.Default.Security,
                titulo = "Privacidad y Seguridad",
                onClick = { }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    loginViewModel.onLogoutClicked {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    Text("Cerrar Sesión", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    // Modal para opciones de imagen
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            icon = {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
            },
            title = {
                Text("Cambiar foto de perfil")
            },
            text = {
                Column {
                    Text("Selecciona una opción para actualizar tu foto de perfil")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                        showImageOptions = false
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PerfilInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ConfiguracionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                titulo,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// ==================== COMPONENTES AUXILIARES ====================
@Composable
fun Chip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when(label) {
            "Pagado", "Resuelto" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            "En proceso" -> Color(0xFFFFA726).copy(alpha = 0.2f)
            else -> Color(0xFFE91E63).copy(alpha = 0.2f)
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when(label) {
                "Pagado", "Resuelto" -> Color(0xFF4CAF50)
                "En proceso" -> Color(0xFFFFA726)
                else -> Color(0xFFE91E63)
            }
        )
    }
}

// ==================== BOTTOM NAVIGATION ====================
@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "home"),
        BottomNavItem("Pagos", Icons.Default.AttachMoney, "pagos"),
        BottomNavItem("Incidencias", Icons.Default.Warning, "incidencias"),
        BottomNavItem("Mensajes", Icons.Default.Message, "mensajes"),
        BottomNavItem("Perfil", Icons.Default.Person, "perfil")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)