package com.example.urbane.ui.admin.fines.view

import com.example.urbane.ui.admin.fines.view.components.FineCard
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.R
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
                    contentDescription = stringResource(R.string.agregar_multa)
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
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = {
                    Text(stringResource(R.string.buscar_por_t_tulo_o_residente), color = Color.Gray)
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
                            text = state.errorMessage ?:stringResource(R.string.error_desconocido) ,
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
                            text = stringResource(R.string.no_hay_multas_registradas),
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
