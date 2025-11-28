package com.example.urbane.ui.resident.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface

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

// Al final de IncidenciasScreen.kt

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