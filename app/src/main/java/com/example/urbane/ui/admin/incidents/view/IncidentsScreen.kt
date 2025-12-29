package com.example.urbane.ui.admin.incidents.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.urbane.ui.admin.incidents.model.IncidentsIntent
import com.example.urbane.ui.admin.incidents.view.components.AttendIncidentBottomSheet
import com.example.urbane.ui.admin.incidents.view.components.IncidentCard
import com.example.urbane.ui.admin.incidents.viewmodel.IncidentsViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsScreen(
    viewModel: IncidentsViewModel
) {
    val state by viewModel.state.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadIncidents()
    }

    LaunchedEffect(state.selectedIncident) {
        showBottomSheet = state.selectedIncident != null
    }

    LaunchedEffect(state.success) {
        if (state.success == true) {
            showBottomSheet = false
        }
    }

    val categoriesWithAll = remember(state.categories) {
        listOf("Todos") + state.categories.map { it.name }
    }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar incidencias",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadIncidents() }) {
                            Text("Reintentar")
                        }
                    }
                }

                state.incidents.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay incidencias",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Las incidencias aparecerán aquí cuando se registren",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // FilterChips dinámicos
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoriesWithAll) { category ->
                                FilterChip(
                                    selected = filtroSeleccionado == category,
                                    onClick = { filtroSeleccionado = category },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Filtrar incidencias
                        val incidenciasFiltradas = remember(state.incidents, filtroSeleccionado) {
                            if (filtroSeleccionado == "Todos") {
                                state.incidents
                            } else {
                                state.incidents.filter { it.category == filtroSeleccionado }
                            }
                        }

                        // Lista de incidencias
                        if (incidenciasFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "No hay incidencias ${filtroSeleccionado.lowercase()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6B7280)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Intenta ajustar tu filtro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = incidenciasFiltradas,
                                    key = { it.id ?: it.hashCode() }
                                ) { incident ->
                                    IncidentCard(
                                        incident = incident,
                                        onAttendClick = {
                                            viewModel.handleIntent(IncidentsIntent.SelectIncident(it))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        AttendIncidentBottomSheet(
            viewModel = viewModel,
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false }
        )
    }
}