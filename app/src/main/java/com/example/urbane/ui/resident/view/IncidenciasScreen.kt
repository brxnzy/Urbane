package com.example.urbane.ui.resident.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.urbane.ui.resident.viewmodel.IncidenciasViewModel
import com.example.urbane.ui.resident.viewmodel.IncidenciasUiState
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidenciasScreen(
    residentId: String,
    residentialId: Int,
    viewModel: IncidenciasViewModel = viewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var errorToast by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        Log.d("IncidenciasScreen", "Iniciando pantalla")
        Log.d("IncidenciasScreen", "ResidentId: $residentId")
        Log.d("IncidenciasScreen", "ResidentialId: $residentialId")
        viewModel.loadIncidencias()
    }

    LaunchedEffect(errorToast) {
        errorToast?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            errorToast = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incidencias") },
                actions = {
                    IconButton(onClick = { viewModel.loadIncidencias() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("IncidenciasScreen", "FAB clicked")
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva incidencia")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is IncidenciasUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is IncidenciasUiState.Success -> {
                    if (state.incidencias.isEmpty()) {
                        EmptyIncidenciasState(
                            modifier = Modifier.align(Alignment.Center),
                            onCreateClick = { showBottomSheet = true }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.incidencias) { incidencia ->
                                IncidenciaCard(
                                    incidencia = incidencia,
                                    residentId = residentId,
                                    viewModel = viewModel,
                                    onDeleteSuccess = { message ->
                                        errorToast = message
                                    }
                                )
                            }
                        }
                    }
                }
                is IncidenciasUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadIncidencias() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        Log.d("IncidenciasScreen", "Mostrando BottomSheet")
        NuevaIncidenciaBottomSheet(
            onDismiss = {
                Log.d("IncidenciasScreen", "BottomSheet cerrado")
                showBottomSheet = false
            },
            onConfirm = { titulo, descripcion, tipo, imageUris ->
                Log.d("IncidenciasScreen", "Confirmando - Titulo: $titulo, Tipo: $tipo, Imágenes: ${imageUris.size}")
                viewModel.createIncidencia(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    residentId = residentId,
                    residentialId = residentialId,
                    imageUris = imageUris,
                    context = context,
                    onSuccess = {
                        Log.d("IncidenciasScreen", "Incidencia creada exitosamente")
                        showBottomSheet = false
                        errorToast = "Incidencia creada exitosamente"
                    },
                    onError = { error ->
                        Log.e("IncidenciasScreen", "Error al crear incidencia: $error")
                        errorToast = "Error: $error"
                    }
                )
            },
            isCreating = viewModel.isCreating.collectAsState().value,
            uploadProgress = viewModel.uploadProgress.collectAsState().value
        )
    }
}

@Composable
fun EmptyIncidenciasState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No hay incidencias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Presiona el botón + para reportar una nueva",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Error al cargar incidencias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun IncidenciaCard(
    incidencia: com.example.urbane.ui.resident.viewmodel.Incidencia,
    residentId: String,
    viewModel: IncidenciasViewModel,
    onDeleteSuccess: (String) -> Unit
) {
    val status = incidencia.status ?: "Pendiente"
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
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
                            when(status) {
                                "Resuelto" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                "En proceso" -> Color(0xFFFFA726).copy(alpha = 0.2f)
                                else -> Color(0xFFE91E63).copy(alpha = 0.2f)
                            }
                        )
                        .padding(8.dp),
                    tint = when(status) {
                        "Resuelto" -> Color(0xFF4CAF50)
                        "En proceso" -> Color(0xFFFFA726)
                        else -> Color(0xFFE91E63)
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        incidencia.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        incidencia.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        incidencia.type?.let { tipo ->
                            Text(
                                "📁 $tipo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        incidencia.createdAt?.let { fecha ->
                            Text(
                                "• ${getRelativeTime(fecha)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Chip(label = status)

                    // Botón eliminar solo si es del residente actual
                    if (incidencia.residentId == residentId) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Mostrar imágenes si existen
            incidencia.incidentImages?.takeIf { it.isNotEmpty() }?.let { images ->
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { image ->
                        AsyncImage(
                            model = image.imageUrl,
                            contentDescription = "Imagen de incidencia",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar incidencia") },
            text = { Text("¿Estás seguro de que deseas eliminar esta incidencia? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        incidencia.id?.let { id ->
                            viewModel.deleteIncidencia(
                                incidenciaId = id,
                                residentId = residentId,
                                onSuccess = {
                                    showDeleteDialog = false
                                    onDeleteSuccess("Incidencia eliminada exitosamente")
                                },
                                onError = { error ->
                                    showDeleteDialog = false
                                    onDeleteSuccess("Error al eliminar: $error")
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

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
            fontWeight = FontWeight.SemiBold,
            color = when(label) {
                "Pagado", "Resuelto" -> Color(0xFF4CAF50)
                "En proceso" -> Color(0xFFFFA726)
                else -> Color(0xFFE91E63)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaIncidenciaBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, List<Uri>) -> Unit,
    isCreating: Boolean,
    uploadProgress: Float
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf<String?>(null) }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var showCategoriaDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
        errorMessage = ""
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isCreating) onDismiss() },
        sheetState = sheetState,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            item {
                Text(
                    "Nueva Incidencia",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Reporta cualquier problema o situación que requiera atención",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    "Tipo de incidencia",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val tipos = listOf(
                    TipoIncidencia("🔧", "Mantenimiento"),
                    TipoIncidencia("🔒", "Seguridad"),
                    TipoIncidencia("💧", "Servicios")
                )

                val tipos2 = listOf(
                    TipoIncidencia("🏢", "Áreas comunes"),
                    TipoIncidencia("📝", "Otros")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tipos.forEach { tipo ->
                        TipoIncidenciaCard(
                            tipo = tipo,
                            isSelected = tipoSeleccionado == tipo.nombre,
                            onClick = {
                                tipoSeleccionado = tipo.nombre
                                categoriaPersonalizada = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tipos2.forEach { tipo ->
                        TipoIncidenciaCard(
                            tipo = tipo,
                            isSelected = tipoSeleccionado == tipo.nombre ||
                                    (tipo.nombre == "Otros" && categoriaPersonalizada.isNotEmpty()),
                            onClick = {
                                if (tipo.nombre == "Otros") {
                                    showCategoriaDialog = true
                                } else {
                                    tipoSeleccionado = tipo.nombre
                                    categoriaPersonalizada = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (categoriaPersonalizada.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Categoría: $categoriaPersonalizada",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    categoriaPersonalizada = ""
                                    tipoSeleccionado = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar categoría",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Text(
                    "Imágenes (mínimo 3)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Selector de imágenes
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 16.dp)
                        .clickable(enabled = !isCreating) {
                            imagePickerLauncher.launch("image/*")
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (selectedImages.size >= 3)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    if (selectedImages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar imágenes",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Toca para seleccionar imágenes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(selectedImages) { uri ->
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Imagen seleccionada",
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedImages = selectedImages.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            item {
                                Card(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clickable { imagePickerLauncher.launch("image/*") },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Agregar más",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    "${selectedImages.size} de 3 imágenes mínimas",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedImages.size >= 3)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        errorMessage = ""
                    },
                    label = { Text("Título") },
                    placeholder = { Text("Ej: Fuga de agua en estacionamiento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !isCreating,
                    isError = titulo.isBlank() && errorMessage.isNotEmpty(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        errorMessage = ""
                    },
                    label = { Text("Descripción") },
                    placeholder = { Text("Describe el problema con detalle...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    minLines = 4,
                    maxLines = 6,
                    enabled = !isCreating,
                    isError = descripcion.isBlank() && errorMessage.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isCreating && uploadProgress > 0) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            "Subiendo imágenes...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = uploadProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Button(
                    onClick = {
                        Log.d("IncidenciasScreen", "Botón Reportar clicked")
                        when {
                            titulo.isBlank() -> {
                                errorMessage = "El título es obligatorio"
                                Log.w("IncidenciasScreen", "Título vacío")
                            }
                            descripcion.isBlank() -> {
                                errorMessage = "La descripción es obligatoria"
                                Log.w("IncidenciasScreen", "Descripción vacía")
                            }
                            tipoSeleccionado == null && categoriaPersonalizada.isBlank() -> {
                                errorMessage = "Selecciona un tipo de incidencia"
                                Log.w("IncidenciasScreen", "Tipo no seleccionado")
                            }
                            selectedImages.size < 3 -> {
                                errorMessage = "Debes agregar al menos 3 imágenes"
                                Log.w("IncidenciasScreen", "Menos de 3 imágenes")
                            }
                            else -> {
                                val tipoFinal = if (categoriaPersonalizada.isNotEmpty())
                                    categoriaPersonalizada
                                else
                                    tipoSeleccionado
                                Log.d("IncidenciasScreen", "Llamando onConfirm con tipo: $tipoFinal")
                                onConfirm(titulo, descripcion, tipoFinal, selectedImages)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isCreating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Reportar incidencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showCategoriaDialog) {
        CategoriaPersonalizadaDialog(
            onDismiss = { showCategoriaDialog = false },
            onConfirm = { categoria ->
                categoriaPersonalizada = categoria
                tipoSeleccionado = "Otros"
                showCategoriaDialog = false
            }
        )
    }
}

@Composable
fun CategoriaPersonalizadaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categoría personalizada") },
        text = {
            Column {
                Text(
                    "Ingresa el nombre de la categoría",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = {
                        texto = it
                        error = false
                    },
                    label = { Text("Categoría") },
                    placeholder = { Text("Ej: Problema eléctrico") },
                    isError = error,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text(
                        "La categoría no puede estar vacía",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (texto.isBlank()) {
                        error = true
                    } else {
                        onConfirm(texto)
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

data class TipoIncidencia(
    val emoji: String,
    val nombre: String
)

@Composable
fun TipoIncidenciaCard(
    tipo: TipoIncidencia,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                tipo.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                tipo.nombre,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2
            )
        }
    }
}

private fun getRelativeTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Instant.now()
        val days = ChronoUnit.DAYS.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val minutes = ChronoUnit.MINUTES.between(instant, now)


        when {
            days > 0 -> "Hace $days ${if (days == 1L) "día" else "días"}"
            hours > 0 -> "Hace $hours ${if (hours == 1L) "hora" else "horas"}"
            minutes > 0 -> "Hace $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
            else -> "Hace un momento"
        }
    } catch (e: Exception) {
        "Fecha desconocida"
    }
}