package com.example.urbane.ui.admin.fines.view
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.fines.model.FinesIntent
import com.example.urbane.ui.admin.fines.model.FinesSuccessType
import com.example.urbane.ui.admin.fines.viewmodel.FinesViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFineScreen(
    viewModel: FinesViewModel,
    goBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expandedResident by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadResidents()
    }

    LaunchedEffect(state.success) {
        if (state.success is FinesSuccessType.FineCreated) {
            snackbarHostState.showSnackbar(
                message = "Multa creada correctamente" ,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            viewModel.handleIntent(FinesIntent.ClearSuccess)

        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crear multa") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = state.title,
                onValueChange = {
                    viewModel.handleIntent(FinesIntent.TitleChanged(it))
                },
                label = { Text("Título de la multa") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = {
                    viewModel.handleIntent(FinesIntent.DescriptionChanged(it))
                },
                label = { Text("Descripción") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !state.isLoading
            )

            OutlinedTextField(
                value = state.amount,
                onValueChange = {
                    viewModel.handleIntent(FinesIntent.AmountChanged(it))
                },
                label = { Text("Monto") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading
            )

            ExposedDropdownMenuBox(
                expanded = expandedResident,
                onExpandedChange = { expandedResident = it && !state.isLoading }
            ) {
                OutlinedTextField(
                    value = state.residents
                        .firstOrNull { it.id == state.selectedResidentId }
                        ?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Residente") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedResident)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !state.isLoading
                )

                ExposedDropdownMenu(
                    expanded = expandedResident,
                    onDismissRequest = { expandedResident = false }
                ) {
                    state.residents.forEach { resident ->
                        DropdownMenuItem(
                            text = { Text(resident.name) },
                            onClick = {
                                viewModel.handleIntent(
                                    FinesIntent.ResidentSelected(resident.id)
                                )
                                expandedResident = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.handleIntent(FinesIntent.CreateFine)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled =
                    state.title.isNotBlank() &&
                            state.amount.isNotBlank() &&
                            state.selectedResidentId != null &&
                            !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear multa")
                }
            }
        }
    }
}
