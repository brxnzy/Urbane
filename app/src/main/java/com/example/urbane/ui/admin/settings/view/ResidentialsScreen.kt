package com.example.urbane.ui.admin.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.ui.admin.settings.model.ResidentialIntent
import com.example.urbane.ui.admin.settings.view.components.ResidentialCard
import com.example.urbane.ui.admin.settings.view.components.ResidentialFormBottomSheet
import com.example.urbane.ui.admin.settings.viewmodel.ResidentialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentialScreen(
    viewModel: ResidentialViewModel,
    goBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.loadResidentials()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mis_residenciales),
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.processIntent(ResidentialIntent.ShowCreateSheet)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    tint = Color.White,
                    contentDescription = "Crear Residencial"
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.residentials.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes residenciales",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.residentials) { residential ->
                        ResidentialCard(
                            residential = residential,
                            onEdit = {
                                viewModel.processIntent(
                                    ResidentialIntent.ShowEditSheet(residential)
                                )
                            }
                        )
                    }
                }
            }

            // Error Snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(
                            onClick = {
                                viewModel.processIntent(ResidentialIntent.DismissError)
                            }
                        ) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }

        // BottomSheet
        if (state.showBottomSheet) {
            ResidentialFormBottomSheet(
                sheetState = sheetState,
                isEditMode = state.isEditMode,
                residential = state.selectedResidential,
                isLoading = state.isLoading, // ✅ Pasar estado de loading
                onDismiss = {
                    viewModel.processIntent(ResidentialIntent.DismissBottomSheet)
                },
                onSave = { name, address, phone, imageUri ->
                    if (state.isEditMode && state.selectedResidential != null) {
                        viewModel.processIntent(
                            ResidentialIntent.UpdateResidential(
                                state.selectedResidential!!.copy(
                                    name = name,
                                    address = address ?: "",
                                    phone = phone ?: ""
                                ),
                                imageUri
                            )
                        )
                    } else {
                        viewModel.processIntent(
                            ResidentialIntent.CreateResidential(
                                name, address, phone, imageUri
                            )
                        )
                    }
                }
            )
        }
    }
}
