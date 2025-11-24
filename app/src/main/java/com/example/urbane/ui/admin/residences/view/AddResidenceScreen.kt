import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.data.model.User
import com.example.urbane.ui.admin.residences.model.ResidencesIntent
import com.example.urbane.ui.admin.residences.model.ResidencesState
import com.example.urbane.ui.admin.residences.model.SuccessType
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.model.UsersIntent
import com.example.urbane.ui.auth.model.RegisterIntent
import com.example.urbane.utils.formatIdCard
import com.example.urbane.utils.isValidEmail
import com.example.urbane.utils.isValidIdCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResidenceScreen(
    viewModel: ResidencesViewModel,
    goBack: () -> Unit
) {
    var perteneceResidencial by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val selectedOwner = state.availableOwners.find { it.id == state.selectedOwnerId }

    val tiposPropiedad = listOf("Apartamento", "Casa", "Local", "Villa", "Terreno")

    // Manejar éxito
    LaunchedEffect(state.success) {
        if (state.success == SuccessType.ResidenceCreated) {
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

        // Bottom Sheet
        if (showBottomSheet) {
            PropietarioBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false },
                onPropietarioSelected = { user ->
                    viewModel.processIntent(ResidencesIntent.SelectOwner(user.id))
                    showBottomSheet = false
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropietarioBottomSheet(
    viewModel: ResidencesViewModel,
    onDismiss: () -> Unit,
    onPropietarioSelected: (User) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsState()

    // Cargar propietarios cuando se abre el bottom sheet
    LaunchedEffect(Unit) {
        viewModel.processIntent(ResidencesIntent.LoadOwners)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Buscar existente") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Crear nuevo") },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> BuscarPropietarioTab(
                    availableOwners = state.availableOwners,
                    isLoading = state.isLoadingOwners,
                    onPropietarioSelected = onPropietarioSelected
                )
                1 -> CrearPropietarioTab(
                    viewModel = viewModel,
                    state = state,
                    onPropietarioCreated = {
                        // Recargar la lista de propietarios
                        viewModel.processIntent(ResidencesIntent.LoadOwners)
                        // Cambiar al tab de buscar
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
fun BuscarPropietarioTab(
    availableOwners: List<User>,
    isLoading: Boolean,
    onPropietarioSelected: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val propietariosFiltrados = availableOwners.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.idCard.toString().contains(searchQuery, ignoreCase = true) ||
                it.email?.contains(searchQuery, ignoreCase = true) == true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(400.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar propietario") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Cargando propietarios...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            availableOwners.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No hay propietarios disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Crea uno en la pestaña 'Crear nuevo'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            propietariosFiltrados.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No se encontraron resultados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(propietariosFiltrados) { propietario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPropietarioSelected(propietario) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = propietario.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Cédula: ${propietario.idCard}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = propietario.email.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrearPropietarioTab(
    viewModel: ResidencesViewModel,
    state: ResidencesState,
    onPropietarioCreated: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFormat by remember { mutableStateOf(true) }
    var idCardFormat by remember { mutableStateOf(true) }
    var validPassword by remember { mutableStateOf(true) }

    LaunchedEffect(state.success) {
        if (state.success == SuccessType.PropietarioAssigned) {
            onPropietarioCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .height(450.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nuevo propietario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = state.ownerName,
            onValueChange = {
                viewModel.processIntent(ResidencesIntent.OwnerNameChanged(it))
            },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isLoading
        )

        OutlinedTextField(
            value = TextFieldValue(
                text = state.ownerIdCard,
                selection = TextRange(state.ownerIdCard.length)
            ),
            onValueChange = { newValue ->
                val digits = newValue.text.replace(Regex("[^0-9]"), "")
                val limited = if (digits.length > 11) digits.substring(0, 11) else digits
                val formatted = formatIdCard(limited)
                idCardFormat = isValidIdCard(formatted)
                viewModel.processIntent(ResidencesIntent.OwnerIdCardChanged(formatted))
            },
            label = { Text("Cédula") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = !state.isLoading
        )

        OutlinedTextField(
            value = state.ownerEmail,
            onValueChange = {
                emailFormat = isValidEmail(it)
                viewModel.processIntent(ResidencesIntent.OwnerEmailChanged(it))
            },
            isError = !emailFormat,
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !state.isLoading
        )

        OutlinedTextField(
            value = state.ownerPassword,
            onValueChange = {
                validPassword = it.length >= 8
                viewModel.processIntent(ResidencesIntent.OwnerPasswordChanged(it))
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            isError = !validPassword,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Ocultar contraseña"
                        else
                            "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isLoading
        )

        if (!validPassword) {
            Text(
                stringResource(R.string.la_contrase_a_debe_contener_al_menos_8_caracteres),
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.processIntent(ResidencesIntent.CreateOwner)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state.ownerName.isNotBlank() &&
                    state.ownerIdCard.isNotBlank() &&
                    state.ownerEmail.isNotBlank() &&
                    state.ownerPassword.isNotBlank() &&
                    !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear y asignar propietario")
            }
        }

        // Mostrar errores
        state.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
@Composable
fun AnimatedVisibility(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    if (visible) {
        content()
    }
}