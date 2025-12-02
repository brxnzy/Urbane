package com.example.urbane.ui.admin.contracts.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.data.model.ContractService
import com.example.urbane.data.model.Service
import com.example.urbane.ui.admin.contracts.model.ContractsDetailIntent
import com.example.urbane.ui.admin.contracts.model.ContractsDetailSuccess
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    contractId: String,
    viewmodel: ContractsDetailViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewmodel.state.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }
    var editedConditions by remember { mutableStateOf("") }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewmodel.loadContract(contractId.toInt())
    }

    LaunchedEffect(state.contract) {
        state.contract?.let { contract ->
            editedConditions = contract.conditions ?: ""
        }
    }

    // Manejar mensajes de éxito
    LaunchedEffect(state.success) {
        state.success?.let { success ->
            val message = when (success) {
                ContractsDetailSuccess.UpdateContract -> "Condiciones actualizadas correctamente"
                ContractsDetailSuccess.AddService -> "Servicio agregado correctamente"
                ContractsDetailSuccess.RemoveService -> "Servicio eliminado correctamente"
            }
            snackbarHostState.showSnackbar(message)
            viewmodel.handleIntent(ContractsDetailIntent.ClearMessages)
        }
    }

    // Manejar mensajes de error
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar("Error: $error")
            viewmodel.handleIntent(ContractsDetailIntent.ClearMessages)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Contrato") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditMode) {
                        // Guardar cambios
                        viewmodel.handleIntent(
                            ContractsDetailIntent.UpdateConditions(
                                contractId = contractId.toInt(),
                                conditions = editedConditions
                            )
                        )
                    }
                    isEditMode = !isEditMode
                },
                containerColor = if (isEditMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditMode) "Guardar" else "Editar"
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.contract?.let { contract ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Información básica no editable
                    InfoSection {
                        ContractInfoItem(
                            label = "Residente",
                            value = contract.residentName ?: "N/A"
                        )

                        ContractInfoItem(
                            label = "Residencia",
                            value = contract.residenceName ?: "N/A"
                        )

                        ContractInfoItem(
                            label = "Fecha de inicio",
                            value = contract.startDate ?: "N/A"
                        )

                        ContractInfoItem(
                            label = "Fecha de fin",
                            value = contract.endDate ?: "N/A"
                        )

                        ContractInfoItem(
                            label = "Estado",
                            value = "Activo",
                            valueColor = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Condiciones especiales
                    Text(
                        text = "Condiciones especiales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editedConditions,
                            onValueChange = { editedConditions = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            placeholder = { Text("Ingrese las condiciones especiales...") },
                            maxLines = 6
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = contract.conditions?.takeIf { it.isNotBlank() }
                                    ?: "Sin condiciones especiales",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (contract.conditions?.isNotBlank() == true)
                                    Color.Black else Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Servicios adicionales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Servicios adicionales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        if (isEditMode) {
                            IconButton(
                                onClick = { showAddServiceDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar servicio",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.contractServices.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = "Sin servicios adicionales",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                state.contractServices.forEach { contractService ->
                                    ServiceItem(
                                        serviceName = contractService.name,
                                        servicePrice = contractService.price,
                                        isEditMode = isEditMode,
                                        onRemove = {
                                            viewmodel.handleIntent(
                                                ContractsDetailIntent.RemoveService(
                                                    contractId = contractId.toInt(),
                                                    contractServiceId = contractService.id
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddServiceDialog) {
        AddServiceDialog(
            availableServices = state.availableServices,
            contractServices = state.contractServices,
            onDismiss = { showAddServiceDialog = false },
            onAdd = { service ->
                viewmodel.handleIntent(
                    ContractsDetailIntent.AddService(
                        contractId = contractId.toInt(),
                        serviceId = service.id
                    )
                )
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
private fun InfoSection(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ContractInfoItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ServiceItem(
    serviceName: String,
    servicePrice: Double,
    isEditMode: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = serviceName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${String.format("%.2f", servicePrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isEditMode) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar servicio",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AddServiceDialog(
    availableServices: List<Service>,
    contractServices: List<ContractService>,
    onDismiss: () -> Unit,
    onAdd: (Service) -> Unit
) {
    Log.d("AddServiceDialog", "Available services: $availableServices")
    val contractServiceIds = contractServices.map { it.serviceId }.toSet()
    val filteredServices = availableServices.filter { it.id !in contractServiceIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Servicio") },
        text = {
            if (filteredServices.isEmpty()) {
                Text("No hay más servicios disponibles para agregar")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredServices.forEach { service ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAdd(service) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", service.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    service.description?.let { desc ->
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}