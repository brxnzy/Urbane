package com.example.urbane.ui.admin.users.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.data.model.Residence
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
import com.example.urbane.ui.admin.users.viewmodel.UsersDetailViewModel

@Composable
fun EnableDialog(goBack: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(stringResource(R.string.usuario_habilitado)) },
        text = {
            Text(
                "Usuario habilitado correctamente"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                goBack()
                onDismiss

            }) {
                Text(stringResource(R.string.aceptar))
            }
        }
    )
}



@Composable
fun DisabledDialog(goBack: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(stringResource(R.string.usuario_deshabilitado)) },
        text = {
            Text(
                stringResource(R.string.usuario_deshabilitado_correctamente)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                goBack()
                onDismiss

            }) {
                Text(stringResource(R.string.aceptar))
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableResidentDialog(
    residencesViewModel: ResidencesViewModel,
    usersDetailViewModel: UsersDetailViewModel,
    closeDialog: ()-> Unit
) {
    LaunchedEffect(Unit) {
        residencesViewModel.loadResidences()
    }
    val state by residencesViewModel.state.collectAsState()

    var expandedTipo by remember { mutableStateOf(false) }
    var expandedResidencia by remember { mutableStateOf(false) }

    var selectedTipo by remember { mutableStateOf("") }
    var selectedResidencia by remember { mutableStateOf<Residence?>(null) }

    val tipos = listOf(stringResource(R.string.casa), stringResource(R.string.apartamento), stringResource(R.string.villa),
        stringResource(R.string.terreno), stringResource(R.string.local))

    val residenciasFiltradas = state.residences.filter {
        it.available && selectedTipo.isNotBlank() && it.type == selectedTipo
    }

    AlertDialog(
        onDismissRequest = closeDialog,
        title = { Text("Asignar residencia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Dropdown TIPO
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = it }
                ) {
                    OutlinedTextField(
                        value = selectedTipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de propiedad") },
                        leadingIcon = { Icon(Icons.Default.Category, null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        tipos.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    selectedTipo = tipo
                                    selectedResidencia = null
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                // Dropdown RESIDENCIA
                ExposedDropdownMenuBox(
                    expanded = expandedResidencia,
                    onExpandedChange = { expandedResidencia = it }
                ) {
                    OutlinedTextField(
                        value = selectedResidencia?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedTipo.isNotBlank(),
                        label = { Text("Residencia") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedResidencia)
                        },
                        placeholder = { Text("Selecciona una residencia") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedResidencia,
                        onDismissRequest = { expandedResidencia = false }
                    ) {
                        if (residenciasFiltradas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay residencias disponibles") },
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            residenciasFiltradas.forEach { residencia ->
                                DropdownMenuItem(
                                    text = { Text(residencia.name) },
                                    onClick = {
                                        selectedResidencia = residencia
                                        expandedResidencia = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedResidencia?.let { residencia ->
                        // Opcional: cerrar diálogo inmediatamente
                        closeDialog()

                        // Actualizar viewmodel y lanzar la acción
                        usersDetailViewModel.setResidenceId(residencia.id)
                        usersDetailViewModel.processIntent(UsersDetailIntent.EnableUser)
                    }
                },
                enabled = selectedTipo.isNotBlank() && selectedResidencia != null
            ) {
                Text("Aceptar")
            }
        },

                dismissButton = {
            TextButton(onClick = closeDialog) {
                Text("Cancelar")
            }
        }
    )
}



