package com.example.urbane.ui.admin.residences.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.data.model.Residence
import com.example.urbane.ui.common.InfoSection
import com.example.urbane.ui.common.UserInfoItem
import com.example.urbane.utils.getResidenceIcon


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
                text = residence.type,
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
                        label = { Text(stringResource(R.string.nombre)) },
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
                            label = { Text(stringResource(R.string.tipo_de_propiedad)) },
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
                        label = { Text(stringResource(R.string.descripci_n)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        minLines = 1,
                        maxLines = 3,
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                    )
                }
            } else {
                UserInfoItem(
                    label = stringResource(R.string.descripci_n),
                    value = residence.description
                )
            }

            UserInfoItem(
                label = "Estado",
                value = if (residence.available) stringResource(R.string.disponible) else stringResource(R.string.ocupada),
                valueColor = if (residence.available == true)
                    MaterialTheme.colorScheme.primary
                else
                    Color(0xFFFF9800)
            )

            UserInfoItem(
                label = stringResource(R.string.residente),
                value = residence.residentName ?: stringResource(R.string.sin_residente)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isEditing) {
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
                    Text(stringResource(R.string.guardar_cambios), style = MaterialTheme.typography.titleMedium)
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
                    Text(stringResource(R.string.cancelar), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.editar), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.desalojar), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.editar), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(R.string.eliminar), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

