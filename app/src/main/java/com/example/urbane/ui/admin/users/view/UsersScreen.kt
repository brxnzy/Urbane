package com.example.urbane.ui.admin.users.view

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Usuario(
    val nombre: String,
    val activo: Boolean,
    val tipo: String
)

@Composable
fun UsersScreen(modifier: Modifier) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }

    val usuarios = listOf(
        Usuario("Juan Pérez", true, "Residente"),
        Usuario("Ana García", false, "Guardia"),
        Usuario("Carlos Rodríguez", true, "Residente"),
        Usuario("Luisa Martínez", true, "Residente"),
        Usuario("Pedro Sánchez", true, "Guardia"),
        Usuario("María López", false, "Residente")
    )

    val filtros = listOf("Todos", "Residente", "Guardia", "Activo")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por nombre...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )

            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = { filtroSeleccionado = filtro },
                        label = { Text(filtro) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }

            // Lista de usuarios
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(usuarios) { usuario ->
                    UsuarioCard(usuario)
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se encontraron más usuarios",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            "Intenta ajustar tu búsqueda o filtros.",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }


@Composable
fun UsuarioCard(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (usuario.tipo == "Residente") Color(0xFFE3F2FD) else Color(0xFFE8EAF6),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (usuario.tipo == "Residente") Icons.Default.Home else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (usuario.tipo == "Residente") Color(0xFF2196F3) else Color(0xFF5C6BC0)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y estado
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    usuario.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,

                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (usuario.activo) Color(0xFF4CAF50) else Color(0xFFF44336),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (usuario.activo) "Activo" else "Inactivo",
                        fontSize = 14.sp,
                        color = if (usuario.activo) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            // Acciones
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}