import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.residences.model.ResidencesIntent
import com.example.urbane.ui.admin.residences.model.ResidenceSuccessType
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResidenceScreen(
    viewModel: ResidencesViewModel,
    goBack: () -> Unit
) {
    var perteneceResidencial by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tiposPropiedad = listOf("Apartamento", "Casa", "Local", "Villa", "Terreno")

    LaunchedEffect(state.success) {
        if (state.success == ResidenceSuccessType.ResidenceCreated) {
            snackbarHostState.showSnackbar(
                message = "Propiedad creada correctamente",
                withDismissAction = true
            )
            viewModel.clearSuccess()
            viewModel.loadResidences()
        }
    }

    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crear Propiedad") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = state.name,
                onValueChange = {
                    viewModel.processIntent(ResidencesIntent.NameChanged(it))
                },
                label = { Text("Nombre de la propiedad") },
                leadingIcon = { Icon(Icons.Default.Home, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = it && !state.isLoading }
            ) {
                OutlinedTextField(
                    value = state.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de propiedad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                    },
                    leadingIcon = { Icon(Icons.Default.Category, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !state.isLoading
                )

                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    tiposPropiedad.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.processIntent(
                                    ResidencesIntent.TypeChanged(tipo)
                                )
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = {
                    viewModel.processIntent(
                        ResidencesIntent.DescriptionChanged(it)
                    )
                },
                label = { Text("Descripción") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.processIntent(ResidencesIntent.CreateResidence)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled =
                    state.name.isNotBlank() &&
                            state.type.isNotBlank() &&
                            state.description.isNotBlank() &&
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
                    Text("Guardar propiedad")
                }
            }
        }
    }
}


