package com.example.urbane.ui.admin.fines.view
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.urbane.R
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
    val multaCreada = stringResource(R.string.multa_creada_correctamente)
    LaunchedEffect(state.success) {
        if (state.success is FinesSuccessType.FineCreated) {
            snackbarHostState.showSnackbar(
                message = multaCreada,
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
                title = { Text(stringResource(R.string.crear_multa)) },
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
                label = { Text(stringResource(R.string.t_tulo_de_la_multa)) },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = {
                    viewModel.handleIntent(FinesIntent.DescriptionChanged(it))
                },
                label = { Text(stringResource(R.string.descripci_n)) },
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
                label = { Text(stringResource(R.string.monto)) },
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
                    label = { Text(stringResource(R.string.residente)) },
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
                    Text(stringResource(R.string.guardar))
                }
            }
        }
    }
}
