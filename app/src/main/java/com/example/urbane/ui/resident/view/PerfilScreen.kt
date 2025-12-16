// ============ PerfilScreen.kt ============
package com.example.urbane.ui.resident.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.urbane.data.local.SessionManager
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.resident.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    loginViewModel: LoginViewModel,
    navController: NavController,
    innerNavController: NavController,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val context = LocalContext.current
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value?.userData?.user
    val uiState by perfilViewModel.uiState.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }

    // Cargar datos del perfil al iniciar
    LaunchedEffect(user?.id) {
        println("👤 Usuario detectado: id=${user?.id}, name=${user?.name}, email=${user?.email}")
        user?.id?.let { userId ->
            println("🔄 Iniciando carga de perfil...")
            perfilViewModel.loadUserProfile(userId)
        } ?: run {
            println("⚠️ No hay userId disponible")
        }
    }

    // Sincronizar el input con el estado
    LaunchedEffect(uiState.name) {
        println("📝 Estado del nombre actualizado: ${uiState.name}")
        if (!isEditMode) {
            nameInput = uiState.name
        }
    }

    // Mostrar mensajes de error y éxito
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            println("❌ Error mostrado: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            perfilViewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            println("✅ Éxito mostrado: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            perfilViewModel.clearMessages()
            if (isEditMode) {
                isEditMode = false
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            user?.id?.let { userId ->
                perfilViewModel.uploadProfilePicture(context, userId, selectedUri)
            }
        }
    }

    Scaffold(
        topBar = {
            if (isEditMode) {
                TopAppBar(
                    title = {
                        Text(
                            "Editar Perfil",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isEditMode = false
                            nameInput = uiState.name
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isEditMode) Modifier.padding(padding) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 4.dp
                )
            }
        } else if (isEditMode) {
            // ============ MODO EDICIÓN ============
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Información Personal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Actualiza tu información. Los campos con * son obligatorios.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    EditTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = "Nombre Completo",
                        icon = Icons.Default.Person,
                        placeholder = "Ingresa tu nombre completo",
                        isRequired = true
                    )
                }

                item {
                    EditTextField(
                        value = uiState.email,
                        onValueChange = { },
                        label = "Correo Electrónico",
                        icon = Icons.Default.Email,
                        enabled = false,
                        helperText = "El correo no puede ser modificado"
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Button(
                        onClick = {
                            user?.id?.let { userId ->
                                perfilViewModel.updateUserName(userId, nameInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = if (!uiState.isSaving && nameInput.isNotBlank()) 8.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = MaterialTheme.colorScheme.primary
                            ),
                        enabled = !uiState.isSaving && nameInput.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Guardar Cambios",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            isEditMode = false
                            nameInput = uiState.name
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                    ) {
                        Text(
                            "Cancelar",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // ============ MODO VISUALIZACIÓN ============
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Header con foto y nombre
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Fondo degradado decorativo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(32.dp))

                                Box(
                                    modifier = Modifier.size(120.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    // Foto de perfil
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shadow(
                                                elevation = 12.dp,
                                                shape = CircleShape,
                                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                    ) {
                                        if (uiState.profileImageUrl != null) {
                                            AsyncImage(
                                                model = uiState.profileImageUrl,
                                                contentDescription = "Foto de perfil",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                    .clickable { showImageOptions = true },
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.tertiary
                                                            )
                                                        )
                                                    )
                                                    .clickable { showImageOptions = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = uiState.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                                                    style = MaterialTheme.typography.displayMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Indicador de carga
                                    if (uiState.isUploading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(48.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 4.dp
                                        )
                                    }

                                    // Botón cámara mejorado
                                    FloatingActionButton(
                                        onClick = { showImageOptions = true },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = CircleShape,
                                                spotColor = MaterialTheme.colorScheme.primary
                                            ),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        elevation = FloatingActionButtonDefaults.elevation(
                                            defaultElevation = 0.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = "Cambiar foto",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = uiState.name.ifBlank { "Usuario" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.email.ifBlank { "Sin correo" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Sección de información
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Información Personal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    PerfilInfoCard(
                        icon = Icons.Default.Person,
                        label = "Nombre Completo",
                        value = uiState.name.ifBlank { "No disponible" }
                    )
                }

                item {
                    PerfilInfoCard(
                        icon = Icons.Default.Email,
                        label = "Correo Electrónico",
                        value = uiState.email.ifBlank { "No disponible" }
                    )
                }

                // Sección de configuración
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Configuración de Cuenta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    ConfiguracionItem(
                        icon = Icons.Default.Edit,
                        titulo = "Editar Información Personal",
                        subtitulo = "Actualiza tu nombre y datos",
                        onClick = { isEditMode = true }
                    )
                }

                item {
                    ConfiguracionItem(
                        icon = Icons.Default.Lock,
                        titulo = "Cambiar Contraseña",
                        subtitulo = "Gestiona tu seguridad",
                        onClick = { innerNavController.navigate("cambiar_contrasena") }
                    )
                }

                item {
                    ConfiguracionItem(
                        icon = Icons.Default.Help,
                        titulo = "Ayuda y Soporte",
                        subtitulo = "¿Necesitas ayuda?",
                        onClick = { }
                    )
                }

                item {
                    ConfiguracionItem(
                        icon = Icons.Default.Security,
                        titulo = "Privacidad y Seguridad",
                        subtitulo = "Configura tu privacidad",
                        onClick = { innerNavController.navigate("privacidad_seguridad") }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = MaterialTheme.colorScheme.error
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar sesión",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Cerrar Sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Diálogo mejorado para cambiar foto
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Cambiar foto de perfil",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Selecciona una imagen de tu galería para actualizar tu foto de perfil",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                        showImageOptions = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Galería", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImageOptions = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ============ COMPOSABLES DE UI ============

@Composable
fun PerfilInfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ConfiguracionItem(
    icon: ImageVector,
    titulo: String,
    subtitulo: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitulo.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    enabled: Boolean = true,
    isRequired: Boolean = false,
    helperText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontWeight = FontWeight.Medium)
                    if (isRequired) {
                        Text(
                            " *",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )

        if (helperText != null) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}