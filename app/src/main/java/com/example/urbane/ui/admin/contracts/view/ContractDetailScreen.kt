package com.example.urbane.ui.admin.contracts.view

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.data.model.ContractService
import com.example.urbane.data.model.Service
import com.example.urbane.ui.admin.contracts.model.ContractsDetailIntent
import com.example.urbane.ui.admin.contracts.model.ContractsDetailSuccess
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsDetailViewModel

@SuppressLint("DefaultLocale")
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
    var editedAmount by remember { mutableStateOf("") }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewmodel.loadContract(contractId.toInt())
    }

    LaunchedEffect(state.contract) {
        state.contract?.let { contract ->
            editedConditions = contract.conditions ?: ""
            editedAmount = contract.amount?.toString() ?: "0.0"
        }
    }
    val contratoActualizado = stringResource(R.string.contrato_actualizado_correctamente)
    val servicioAgregado = stringResource(R.string.servicio_agregado_correctamente)
    val servicioEliminado = stringResource(R.string.servicio_eliminado_correctamente)


    LaunchedEffect(state.success) {
        state.success?.let { success ->
            val message = when (success) {
                ContractsDetailSuccess.UpdateContract -> contratoActualizado
                ContractsDetailSuccess.AddService -> servicioAgregado
                ContractsDetailSuccess.RemoveService -> servicioEliminado
            }
            snackbarHostState.showSnackbar(message)
            viewmodel.handleIntent(ContractsDetailIntent.ClearMessages)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar("Error: $error")
            viewmodel.handleIntent(ContractsDetailIntent.ClearMessages)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_del_contrato)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.volver))
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
                        // Verificar si hubo cambios reales
                        val originalConditions = state.contract?.conditions ?: ""
                        val originalAmount = state.contract?.amount ?: 0.0
                        val newAmount = editedAmount.toDoubleOrNull() ?: 0.0

                        val hasChanges = editedConditions != originalConditions ||
                                newAmount != originalAmount

                        if (hasChanges) {
                            viewmodel.handleIntent(
                                ContractsDetailIntent.UpdateContract(
                                    contractId = contractId.toInt(),
                                    conditions = editedConditions,
                                    amount = newAmount
                                )
                            )
                        }
                    }
                    isEditMode = !isEditMode
                },
                containerColor = if (isEditMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditMode) stringResource(R.string.guardar) else stringResource(R.string.editar)
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

                    InfoSection {
                        ContractInfoItem(
                            label = stringResource(R.string.residente),
                            value = contract.residentName ?: "N/A"
                        )

                        ContractInfoItem(
                            label = stringResource(R.string.residencia),
                            value = contract.residenceName ?: "N/A"
                        )

                        ContractInfoItem(
                            label = stringResource(R.string.fecha_de_inicio),
                            value = contract.startDate
                        )

                        ContractInfoItem(
                            label = stringResource(R.string.fecha_de_fin),
                            value = contract.endDate ?: "N/A"
                        )

                        ContractInfoItem(
                            label = stringResource(R.string.estado),
                            value = if(contract.active == true) stringResource(R.string.activo) else stringResource(
                                R.string.inactivo
                            ),
                            valueColor = if(contract.active == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Monto base",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editedAmount,
                            onValueChange = { editedAmount = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.monto)) },
                            prefix = { Text("$") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = "$${String.format("%.2f", contract.amount ?: 0.0)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.condiciones_especiales),
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
                            placeholder = { Text(stringResource(R.string.ingrese_las_condiciones_especiales)) },
                            maxLines = 6
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = contract.conditions?.takeIf { it.isNotBlank() }
                                    ?: stringResource(R.string.sin_condiciones_especiales),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (contract.conditions?.isNotBlank() == true)
                                    Color.Black else Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.servicios_adicionales),
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
                                    contentDescription = stringResource(R.string.agregar_servicio),
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
                                text = stringResource(R.string.sin_servicios_adicionales),
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

                    Spacer(modifier = Modifier.height(24.dp))
                    var totalAmount = state.contract!!.amount ?: 0.0
                    state.contractServices.forEach {
                        totalAmount += it.price
                    }

                    Text(
                        text = "Monto total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {

                        Text(
                            text = "$${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }



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

@SuppressLint("DefaultLocale")
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
                    contentDescription = stringResource(R.string.eliminar_servicio),
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
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
        title = { Text(stringResource(R.string.agregar_servicio)) },
        text = {
            if (filteredServices.isEmpty()) {
                Text(stringResource(R.string.no_hay_m_s_servicios_disponibles_para_agregar))
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
                Text(stringResource(R.string.cerrar))
            }
        }
    )
}