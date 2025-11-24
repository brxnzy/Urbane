package com.example.urbane.ui.admin.residences.view

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesDetailViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close

import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color

import com.example.urbane.data.model.Residence
import com.example.urbane.ui.admin.users.view.components.InfoSection
import com.example.urbane.ui.admin.users.view.components.UserInfoItem
import com.example.urbane.utils.getResidenceIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidencesDetailScreen(
    residenceId: Int,
    viewmodel: ResidencesDetailViewModel,
    goBack: () -> Unit
) {
    val state by viewmodel.state.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    // Estados para edición
    var editedName by remember { mutableStateOf("") }
    var editedType by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var expandedTipo by remember { mutableStateOf(false) }

    // Valores originales para comparación
    var originalName by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf("") }
    var originalDescription by remember { mutableStateOf("") }

    LaunchedEffect(residenceId) {
        viewmodel.loadResidence(residenceId)
    }

    LaunchedEffect(state.residence) {
        state.residence?.let {
            editedName = it.name
            editedType = it.type
            editedDescription = it.description

            // Guardar valores originales
            originalName = it.name
            originalType = it.type
            originalDescription = it.description
        }
    }

    // Verificar si hay cambios
    val hasChanges = editedName != originalName ||
            editedType != originalType ||
            editedDescription != originalDescription

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
                    IconButton(onClick = goBack) {
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
                    editedName = editedName,
                    editedType = editedType,
                    editedDescription = editedDescription,
                    expandedTipo = expandedTipo,
                    hasChanges = hasChanges,
                    onNameChange = { editedName = it },
                    onTypeChange = { editedType = it },
                    onDescriptionChange = { editedDescription = it },
                    onExpandedTipoChange = { expandedTipo = it },
                    onEditClick = { isEditing = true },
                    onCancelEdit = {
                        isEditing = false
                        // Restaurar valores originales
                        editedName = originalName
                        editedType = originalType
                        editedDescription = originalDescription
                    },
                    onSaveEdit = {
                        // TODO: Guardar cambios
                        isEditing = false
                    },
                    onEvictClick = { /* TODO: Desalojar */ },
                    onDeleteClick = { /* TODO: Eliminar */ }
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
    val hasOccupants = residence.ownerId != null || residence.residentId != null
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
                        onExpandedChange = { onExpandedTipoChange(it) }
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
                        modifier = Modifier.fillMaxWidth(),
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
                label = "Propietario",
                value = residence.ownerName ?: "Sin propietario"
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
                enabled = hasChanges // Solo habilitado si hay cambios
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
            // Botones normales
            if (hasOccupants) {
                // Si tiene ocupantes: mostrar Editar y Desalojar
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
                // Si NO tiene ocupantes: mostrar Editar y Eliminar
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


// Asumiendo que estas funciones ya existen en tu código
@Composable
fun InfoSection(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun UserInfoItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}
