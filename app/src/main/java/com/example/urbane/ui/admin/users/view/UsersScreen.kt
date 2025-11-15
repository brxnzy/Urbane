package com.example.urbane.ui.admin.users.view

import android.annotation.SuppressLint
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
import androidx.navigation.NavController
import com.example.urbane.data.model.User
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel


@SuppressLint("SuspiciousIndentation", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UsersScreen(viewmodel: UsersViewModel,modifier: Modifier = Modifier, navController: NavController) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
    val state by viewmodel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewmodel.loadUsers()
    }



    val filtros = listOf("Todos", "Residente", "Guardia", "Activo")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_USERS_ADD) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Agregar usuario")
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()

        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth(),
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

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = { filtroSeleccionado = filtro },
                        label = { Text(filtro) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.activeUsers) { usuario ->
                    UsuarioCard(usuario)
                }

                item {
                    Column(
                        modifier = Modifier.padding(vertical = 32.dp)
                            .fillMaxWidth(),

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
}


@Composable
fun UsuarioCard(usuario: User) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )

    ) {
        Row(
            modifier = Modifier.padding(16.dp)
                .fillMaxWidth(),

            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (usuario.role_name == "resident") Color(0xFFE3F2FD) else Color(0xFFE8EAF6),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (usuario.role_name == "resident") Icons.Default.Home else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (usuario.role_name == "resident") Color(0xFF2196F3) else Color(0xFF5C6BC0)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y estado
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    usuario.name.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,

                )

            }


        }
    }
}