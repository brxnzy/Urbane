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
    var showSuccessDialog by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val tiposPropiedad = listOf("Apartamento", "Casa", "Local", "Villa", "Terreno")

    LaunchedEffect(state.success) {
        if (state.success == ResidenceSuccessType.ResidenceCreated) {
            showSuccessDialog = true
        }
    }

    // Manejar errores
    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Aquí puedes mostrar un Snackbar o Toast
            Log.e("AddResidence", "Error: $error")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Propiedad") },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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

            // Nombre de la propiedad
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.processIntent(ResidencesIntent.NameChanged(it)) },
                label = { Text("Nombre de la propiedad") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading
            )

            // Tipo de propiedad
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = !state.isLoading && it }
            ) {
                OutlinedTextField(
                    value = state.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de propiedad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                    },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                                viewModel.processIntent(ResidencesIntent.TypeChanged(tipo))
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            // Descripción
            OutlinedTextField(
                value = state.description,
                onValueChange = {
                    viewModel.processIntent(ResidencesIntent.DescriptionChanged(it))
                },
                label = { Text("Descripción") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                enabled = !state.isLoading
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))



            Spacer(modifier = Modifier.weight(1f))

            // Botón guardar
            Button(
                onClick = {
                    viewModel.processIntent(ResidencesIntent.CreateResidence)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.name.isNotBlank() &&
                        state.type.isNotBlank() &&
                        state.description.isNotBlank() &&
                        (perteneceResidencial || state.selectedOwnerId != null) &&
                        !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guardar propiedad",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }



        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    viewModel.clearSuccess()
                },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("¡Propiedad creada!") },
                text = {
                    Text(
                        if (perteneceResidencial)
                            "Propiedad registrada como parte del residencial"
                        else
                            "La propiedad ha sido creada exitosamente"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.clearSuccess()
                            viewModel.loadResidences()
                            goBack()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

