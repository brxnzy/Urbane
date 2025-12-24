package com.example.urbane.ui.admin.fines.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urbane.data.model.Fine
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.fines.viewmodel.FinesViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinesScreen(
    viewModel: FinesViewModel,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val filtros = listOf("Todos", "Pendiente", "Pagada", "Cancelada")

    val multasFiltradas = state.fines.filter { fine ->
        val cumpleBusqueda = fine.title.contains(busqueda, ignoreCase = true) ||
                fine.resident?.name?.contains(busqueda, ignoreCase = true) == true

        val cumpleFiltro = when (filtroSeleccionado) {
            "Todos" -> true
            "Pendiente" -> fine.status.equals("pendiente", ignoreCase = true) ||
                    fine.status.equals("pending", ignoreCase = true)
            "Pagada" -> fine.status.equals("pagada", ignoreCase = true) ||
                    fine.status.equals("paid", ignoreCase = true)
            "Cancelada" -> fine.status.equals("cancelada", ignoreCase = true) ||
                    fine.status.equals("cancelled", ignoreCase = true)
            else -> true
        }

        cumpleBusqueda && cumpleFiltro
    }

    LaunchedEffect(Unit) {
        viewModel.loadFines()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_FINES_ADD) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    tint = Color.White,
                    contentDescription = "Agregar multa"
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .padding(horizontal = 16.dp)
        ) {
            // BARRA DE BÚSQUEDA
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = {
                    Text("Buscar por título o residente", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                singleLine = true
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { filtro ->
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

            // CONTENIDO
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
                        Text(
                            text = state.errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                multasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay multas registradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(multasFiltradas) { fine ->
                            FineCard(
                                fine = fine,
                                onClick = { navController.navigate(Routes.ADMIN_FINES_DETAIL.replace(
                                    "{id}",
                                    fine.id.toString()
                                )) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FineCard(
    fine: Fine,
    onClick: () -> Unit
) {
    val status = fine.status.lowercase()

    val yellow = Color(0xFFFFC107)
    val red = Color.Red

    val bgColor = when (status) {
        "pagado", "paid" ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

        "pendiente", "pending" ->
            yellow.copy(alpha = 0.20f)

        "cancelada", "canceled", "cancelled" ->
            red.copy(alpha = 0.20f)

        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when (status) {
        "pagado", "paid" -> Icons.Default.CheckCircle
        "pendiente", "pending" -> Icons.Default.Schedule
        "cancelada", "canceled", "cancelled" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    val iconTint = when (status) {
        "pagado", "paid" ->
            MaterialTheme.colorScheme.primary

        "pendiente", "pending" ->
            yellow

        "cancelada", "canceled", "cancelled" ->
            red

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ICONO
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            // INFO
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fine.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                fine.resident?.let { resident ->
                    Text(
                        text = resident.name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = bgColor
                    ) {
                        Text(
                            text = fine.status.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = iconTint,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }

                    if (fine.paymentId == null && status == "pendiente") {
                        Text(
                            text = "⚠ Próximo pago",
                            fontSize = 11.sp,
                            color = yellow
                        )
                    }
                }
            }

            // MONTO
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.2f", fine.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // FLECHA
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
