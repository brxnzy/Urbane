import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

data class Propietario(
    val id: String,
    val nombre: String,
    val cedula: String,
    val correo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResidenceScreen(goBack:()->Unit) {
    var nombrePropiedad by remember { mutableStateOf("") }
    var tipoPropiedad by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var perteneceResidencial by remember { mutableStateOf(false) }
    var propietarioSeleccionado by remember { mutableStateOf<Propietario?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }

    val tiposPropiedad = listOf("Apartamento", "Casa", "Local", "Oficina", "Penthouse")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Propiedad") },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }

                }


            )}
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
                value = nombrePropiedad,
                onValueChange = { nombrePropiedad = it },
                label = { Text("Nombre o número de propiedad") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tipo de propiedad - Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = it }
            ) {
                OutlinedTextField(
                    value = tipoPropiedad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de propiedad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    tiposPropiedad.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoPropiedad = tipo
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            // Dirección
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección o bloque") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Switch: ¿Pertenece al residencial?
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (perteneceResidencial)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Pertenece al residencial?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (perteneceResidencial)
                                "Propiedad del residencial - Sin propietario"
                            else
                                "Requiere asignar un propietario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = perteneceResidencial,
                        onCheckedChange = {
                            perteneceResidencial = it
                            if (it) propietarioSeleccionado = null
                        }
                    )
                }
            }

            // Sección de propietario
            AnimatedVisibility(visible = !perteneceResidencial) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Propietario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (propietarioSeleccionado != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = propietarioSeleccionado!!.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = propietarioSeleccionado!!.cedula,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { propietarioSeleccionado = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover")
                                }
                            }
                        } else {
                            Button(
                                onClick = { showBottomSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Asignar propietario")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón guardar
            Button(
                onClick = {
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nombrePropiedad.isNotBlank() &&
                        tipoPropiedad.isNotBlank() &&
                        direccion.isNotBlank() &&
                        (perteneceResidencial || propietarioSeleccionado != null)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar propiedad", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Bottom Sheet para seleccionar/crear propietario
        if (showBottomSheet) {
            PropietarioBottomSheet(
                onDismiss = { showBottomSheet = false },
                onPropietarioSelected = { propietario ->
                    propietarioSeleccionado = propietario
                    showBottomSheet = false
                }
            )
        }

        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
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
                    TextButton(onClick = { showSuccessDialog = false }) {
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
    onDismiss: () -> Unit,
    onPropietarioSelected: (Propietario) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                0 -> BuscarPropietarioTab(onPropietarioSelected)
                1 -> CrearPropietarioTab(onPropietarioSelected)
            }
        }
    }
}

@Composable
fun BuscarPropietarioTab(onPropietarioSelected: (Propietario) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    // Lista de ejemplo
    val propietariosEjemplo = remember {
        listOf(
            Propietario("1", "Juan Pérez", "001-1234567-8", "juan@email.com"),
            Propietario("2", "María González", "001-8765432-1", "maria@email.com"),
            Propietario("3", "Carlos Rodríguez", "001-2468135-7", "carlos@email.com")
        )
    }

    val propietariosFiltrados = propietariosEjemplo.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.cedula.contains(searchQuery, ignoreCase = true)
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
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            propietariosFiltrados.forEach { propietario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                                text = propietario.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = propietario.cedula,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = propietario.correo,
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

@Composable
fun CrearPropietarioTab(onPropietarioCreated: (Propietario) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = cedula,
            onValueChange = { cedula = it },
            label = { Text("Cédula") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val nuevoPropietario = Propietario(
                    id = System.currentTimeMillis().toString(),
                    nombre = nombre,
                    cedula = cedula,
                    correo = correo
                )
                onPropietarioCreated(nuevoPropietario)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = nombre.isNotBlank() &&
                    cedula.isNotBlank() &&
                    correo.isNotBlank() &&
                    contrasena.isNotBlank()
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear y asignar propietario")
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