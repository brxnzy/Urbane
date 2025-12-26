package com.example.urbane.ui.admin.residences.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.urbane.data.model.Residence
import com.example.urbane.ui.admin.residences.model.ResidencesDetailIntent
import com.example.urbane.ui.admin.residences.model.ResidencesDetailSuccess
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesDetailViewModel
import com.example.urbane.utils.getResidenceIcon
import com.example.urbane.ui.common.UserInfoItem
import com.example.urbane.ui.common.InfoSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidencesDetailScreen(
    residenceId: Int,
    viewmodel: ResidencesDetailViewModel,
    goBack: (showDeleteMessage: Boolean) -> Unit // Cambiado para pasar parámetro
) {
    val state by viewmodel.state.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mensajes de snackbar
    val residenciaEditada = "Residencia editada exitosamente"
    val residenciaDesalojada = "Residencia desalojada exitosamente"

    LaunchedEffect(residenceId) {
        viewmodel.loadResidence(residenceId)
    }


    LaunchedEffect(state.success, state.isLoading) {
        if (state.success != null && !state.isLoading) {
            when (state.success) {
                ResidencesDetailSuccess.ResidenceEdited -> {
                    snackbarHostState.showSnackbar(
                        message = residenciaEditada,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    isEditing = false
                    viewmodel.resetSuccess()
                }

                ResidencesDetailSuccess.ResidenceDeleted -> {
                    // Navegar hacia atrás con el flag de eliminación
                    goBack(true)
                }

                ResidencesDetailSuccess.ResidenceVacated -> {
                    snackbarHostState.showSnackbar(
                        message = residenciaDesalojada,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    viewmodel.resetSuccess()
                }

                null -> {}
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "¿Eliminar residencia?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro que quieres eliminar esta residencia? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewmodel.processIntent(
                            ResidencesDetailIntent.DeleteResidence(id = residenceId)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!state.isLoading && state.residence != null) {
                            "Detalle de ${state.residence!!.name}"
                        } else {
                            "Detalle de Residencia"
                        },
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack(false) }) { // Sin mensaje al volver normal
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${state.errorMessage}")
                }
            }

            state.residence != null -> {
                ResidenceDetail(
                    residence = state.residence!!,
                    modifier = Modifier.padding(paddingValues),
                    isEditing = isEditing,
                    editedName = state.editedName,
                    editedType = state.editedType,
                    editedDescription = state.editedDescription,
                    expandedTipo = expandedTipo,
                    hasChanges = state.hasChanges,
                    onNameChange = { viewmodel.processIntent(ResidencesDetailIntent.UpdateName(it)) },
                    onTypeChange = { viewmodel.processIntent(ResidencesDetailIntent.UpdateType(it)) },
                    onDescriptionChange = { viewmodel.processIntent(ResidencesDetailIntent.UpdateDescription(it)) },
                    onExpandedTipoChange = { expandedTipo = it },
                    onEditClick = { isEditing = true },
                    onCancelEdit = {
                        isEditing = false
                        // Restaurar valores originales
                        viewmodel.processIntent(ResidencesDetailIntent.UpdateName(state.originalName))
                        viewmodel.processIntent(ResidencesDetailIntent.UpdateType(state.originalType))
                        viewmodel.processIntent(ResidencesDetailIntent.UpdateDescription(state.originalDescription))
                    },
                    onSaveEdit = {
                        viewmodel.processIntent(
                            ResidencesDetailIntent.EditResidence(
                                id = residenceId,
                                name = state.editedName,
                                type = state.editedType,
                                description = state.editedDescription
                            )
                        )
                    },
                    onEvictClick = {
                        state.residence?.residentId?.let { residentId ->
                            viewmodel.processIntent(
                                ResidencesDetailIntent.VacateResidence(
                                    id = residenceId,
                                    residentId = residentId
                                )
                            )
                        }
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidenceDetail(
    residence: Residence,
    modifier: Modifier = Modifier,
    isEditing: Boolean,
    editedName: String,
    editedType: String,
    editedDescription: String,
    expandedTipo: Boolean,
    hasChanges: Boolean,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onExpandedTipoChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onEvictClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val hasOccupants = residence.residentId != null
    val tiposPropiedad = listOf("Apartamento", "Casa", "Local", "Villa", "Terreno")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono de la residencia
        Icon(
            imageVector = getResidenceIcon(residence.type),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isEditing) {
            Text(
                text = residence.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = residence.type ?: "Tipo no especificado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sección de información
        InfoSection {
            if (isEditing) {
                // Modo edición con inputs
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = onNameChange,
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedTipo,
                        onExpandedChange = onExpandedTipoChange
                    ) {
                        OutlinedTextField(
                            value = editedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de propiedad") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                            },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { onExpandedTipoChange(false) }
                        ) {
                            tiposPropiedad.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        onTypeChange(tipo)
                                        onExpandedTipoChange(false)
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        minLines = 1,
                        maxLines = 3,
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                    )
                }
            } else {
                UserInfoItem(
                    label = "Descripción",
                    value = residence.description ?: "No disponible"
                )
            }

            UserInfoItem(
                label = "Estado",
                value = if (residence.available == true) "Disponible" else "Ocupada",
                valueColor = if (residence.available == true)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFFFF9800)
            )

            UserInfoItem(
                label = "Residente",
                value = residence.residentName ?: "Sin residente"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones
        if (isEditing) {
            // Botones de edición
            Button(
                onClick = onSaveEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = hasChanges
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCancelEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            if (hasOccupants) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onEvictClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desalojar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}


