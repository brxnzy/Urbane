package com.example.urbane.ui.admin.contracts.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.urbane.navigation.Routes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.ui.admin.contracts.view.components.ContractCard
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ContractsViewModel,

) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadContracts()
    }
    Scaffold(

    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.contracts,
                            key = { it.id ?: it.hashCode() }
                        ) { contract ->
                            ContractCard(
                                contract = contract,
                                onClick = {navController.navigate(Routes.ADMIN_CONTRACTS_DETAIL.replace("{id}", contract.id.toString()))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreenPreview() {
    MaterialTheme {
        Surface {
            Text("Preview - Use ContractsScreen with ViewModel")
        }
    }
}


