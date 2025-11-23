package com.example.urbane.ui.admin.users.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.data.model.Residence
import com.example.urbane.data.model.Role
import com.example.urbane.data.model.User
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: User,
    residences: List<Residence>,
    onDismiss: () -> Unit,
    onConfirm: ((Int, String?, Set<String>) -> Unit)?
) {

    var newRole by remember { mutableStateOf<Int?>(null) }
    var selectedTipoPropiedad by remember { mutableStateOf("") }
    var selectedResidenceId by remember { mutableStateOf<String?>(null) }
    var selectedOwnerResidences by remember { mutableStateOf<Set<String>>(emptySet()) }

    // ESTO FALTABA
    var expandedRol by remember { mutableStateOf(false) }

    val roles = listOf(
        Role(1, stringResource(R.string.role_admin)),
        Role(2, stringResource(R.string.role_resident)),
        Role(3, stringResource(R.string.role_owner))
    )

    val residenciasFiltradas = remember(residences, newRole, selectedTipoPropiedad) {
        when (newRole) {
            2 -> residences.filter { // RESIDENT
                it.available &&
                        selectedTipoPropiedad.isNotBlank() &&
                        it.type == selectedTipoPropiedad
            }

            3 -> residences.filter { // OWNER
                it.ownerId == null &&
                        selectedTipoPropiedad.isNotBlank() &&
                        it.type == selectedTipoPropiedad
            }

            else -> emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar usuario") },
        text = {
            Column {

                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = it }
                ) {

                    OutlinedTextField(
                        value = roles.firstOrNull { it.id == newRole }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        roles
                            .filter { it.id != user.role_id }       // AQUÍ FILTRAS EL ROL ACTUAL
                            .forEach { rol ->
                                DropdownMenuItem(
                                    text = { Text(rol.name) },
                                    onClick = {
                                        newRole = rol.id
                                        expandedRol = false

                                        selectedTipoPropiedad = ""
                                        selectedResidenceId = null
                                        selectedOwnerResidences = emptySet()
                                    }
                                )
                            }
                    }
                }

                if (newRole == 2) {
                    Spacer(Modifier.height(12.dp))

                    DropdownMenuTipos(
                        selected = selectedTipoPropiedad,
                        onSelect = {
                            selectedTipoPropiedad = it
                            selectedResidenceId = null
                        }
                    )

                    if (selectedTipoPropiedad.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        if (residenciasFiltradas.isEmpty()) {
                            Text("No hay propiedades disponibles")
                        } else {
                            DropdownMenuResidencias(
                                selectedId = selectedResidenceId,
                                residencias = residenciasFiltradas,
                                onSelect = { selectedResidenceId = it }
                            )
                        }
                    }
                }

                // --------------------------
                // OWNER
                // --------------------------
                if (newRole == 3) {
                    Spacer(Modifier.height(12.dp))
                    Text("Selecciona las propiedades a asignar como dueño")

                    val residenciasSinOwner = residences.filter { it.ownerId == null }

                    if (residenciasSinOwner.isEmpty()) {
                        Text("No hay propiedades sin dueño disponibles")
                    } else {
                        OwnerResidencesCheckboxList(
                            residencias = residenciasSinOwner,
                            selectedItems = selectedOwnerResidences,
                            onToggle = { residenceId, checked ->
                                selectedOwnerResidences = if (checked) {
                                    selectedOwnerResidences + residenceId
                                } else {
                                    selectedOwnerResidences - residenceId
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = getMensajeTransicion(
                        user.role_name.toString(),
                        roles.firstOrNull { it.id == newRole }?.name ?: ""
                    ),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalRoleId: Int = newRole ?: user.role_id ?: return@Button
                    onConfirm?.invoke(
                        finalRoleId,
                        selectedResidenceId,
                        selectedOwnerResidences
                    )
                },
                enabled = isValidSelectionRoleBased(
                    roleId = newRole,
                    selectedResidenceId = selectedResidenceId,
                    selectedOwnerResidences = selectedOwnerResidences
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


// Validar que la selección sea válida
private fun isValidSelectionRoleBased(
    roleId: Int?,
    selectedResidenceId: String?,
    selectedOwnerResidences: Set<String>
): Boolean {
    return when (roleId) {
        1 -> true                              // admin
        2 -> selectedResidenceId != null       // resident necesita 1 residencia
        3 -> selectedOwnerResidences.isNotEmpty() // owner necesita >=1 checkbox
        else -> false
    }
}


// Mensaje informativo según la transición
private fun getMensajeTransicion(currentRole: String, newRole: String): String {
    return when {
        currentRole == "admin" && newRole == "resident" ->
            "Se asignará una propiedad disponible al usuario"

        currentRole == "admin" && newRole == "owner" ->
            "Se asignarán propiedades sin dueño al usuario"

        currentRole == "resident" && newRole == "admin" ->
            "Se liberará la propiedad actual del residente"

        currentRole == "resident" && newRole == "owner" ->
            "Se liberará su propiedad actual y se asignarán propiedades como dueño"

        currentRole == "owner" && newRole == "admin" ->
            "Se removerá como dueño de todas sus propiedades"

        currentRole == "owner" && newRole == "resident" ->
            "Se removerá como dueño y se asignará una propiedad disponible como residente"

        else -> ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuRoles(
    selected: String,
    roles: List<Role>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.ifBlank { "Seleccionar rol" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Rol") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { rol ->
                DropdownMenuItem(
                    text = { Text(rol.name) },
                    onClick = {
                        onSelect(rol.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuTipos(
    selected: String,
    onSelect: (String) -> Unit
) {
    val tipos = listOf("Apartamento", "Casa", "Local", "Villa", "Terreno")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.ifBlank { "Seleccionar tipo" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tipos.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo) },
                    onClick = {
                        onSelect(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuResidencias(
    selectedId: String?,
    residencias: List<Residence>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedId?.let { id ->
        residencias.find { it.id.toString() == id }?.name
    } ?: "Seleccionar residencia"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Residencia") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            residencias.forEach { res ->
                DropdownMenuItem(
                    text = { Text(res.name) },
                    onClick = {
                        onSelect(res.id.toString())
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerResidencesCheckboxList(
    residencias: List<Residence>,
    selectedItems: Set<String>,
    onToggle: (String, Boolean) -> Unit
) {
    Column {
        residencias.forEach { res ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = selectedItems.contains(res.id.toString()),
                    onCheckedChange = { checked ->
                        onToggle(res.id.toString(), checked)
                    }
                )
                Text(text = "${res.name} - ${res.type}")
            }
        }
    }
}