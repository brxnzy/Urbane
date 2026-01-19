
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
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
import com.example.urbane.ui.admin.residences.model.ResidenceSuccessType
import com.example.urbane.ui.admin.residences.model.ResidencesIntent
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
    val propiedadCreadaCorrectamente = stringResource(R.string.propiedad_creada_correctamente)

    LaunchedEffect(state.success) {
        if (state.success == ResidenceSuccessType.ResidenceCreated) {
            snackbarHostState.showSnackbar(
                message = propiedadCreadaCorrectamente,
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
                title = { Text(stringResource(R.string.crear_propiedad)) },
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
                label = { Text(stringResource(R.string.nombre_de_la_propiedad)) },
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
                    label = { Text(stringResource(R.string.tipo_de_propiedad)) },
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
                label = { Text(stringResource(R.string.descripci_n)) },
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
                    Text(stringResource(R.string.guardar_propiedad))
                }
            }
        }
    }
}


