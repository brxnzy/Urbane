package com.example.urbane.ui.admin.contracts.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.data.model.Contract
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.contracts.view.components.ContractCard
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ContractsViewModel,
) {
    val state by viewModel.state.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    LaunchedEffect(Unit) {
        viewModel.loadContracts()
    }

    val filtros = listOf("Todos", "Activos", "Finalizados")

    // Filtrar los contratos según el filtro seleccionado
    val contratosFiltrados = state.contracts.filter { contract ->
        when (filtroSeleccionado) {
            "Todos" -> true
            "Activos" -> contract.active == true && !isContractExpired(contract)
            "Finalizados" -> contract.active == false
            else -> true
        }
    }

    Scaffold {
        Box(
            modifier = modifier
                .fillMaxSize()

        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar contratos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadContracts() }) {
                            Text("Reintentar")
                        }
                    }
                }

                state.contracts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay contratos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Los contratos aparecerán aquí cuando se registren",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // FilterChips con el mismo estilo que UsersScreen
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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

                        // Lista de contratos filtrados
                        if (contratosFiltrados.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "No hay contratos ${filtroSeleccionado.lowercase()}",
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
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = contratosFiltrados,
                                    key = { it.id ?: it.hashCode() }
                                ) { contract ->
                                    ContractCard(
                                        contract = contract,
                                        onClick = {
                                            navController.navigate(
                                                Routes.ADMIN_CONTRACTS_DETAIL.replace(
                                                    "{id}",
                                                    contract.id.toString()
                                                )
                                            )
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
}

private fun isContractExpired(contract: Contract): Boolean {
    val endDate = contract.endDate ?: return false
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(endDate, formatter)
        date.isBefore(LocalDate.now())
    } catch (e: Exception) {
        false
    }
}
