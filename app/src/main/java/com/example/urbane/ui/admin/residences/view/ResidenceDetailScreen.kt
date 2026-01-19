package com.example.urbane.ui.admin.residences.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.residences.model.ResidencesDetailIntent
import com.example.urbane.ui.admin.residences.model.ResidencesDetailSuccess
import com.example.urbane.ui.admin.residences.view.components.ResidenceDetail
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesDetailViewModel

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


