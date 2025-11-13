package com.example.urbane.ui.admin.users.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.ui.admin.users.model.UsersIntent
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.utils.formatIdCard
import com.example.urbane.utils.isValidEmail
import com.example.urbane.utils.isValidIdCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import com.example.urbane.data.model.Residence
import com.example.urbane.data.model.Role
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(viewModel: UsersViewModel, residencesViewModel: ResidencesViewModel, goBack: () -> Unit) {
    var emailFormat by remember { mutableStateOf(true) }
    var idCardFormat by remember { mutableStateOf(true) }
    var validPassword by remember { mutableStateOf(true) }
    var selectedRol by remember { mutableStateOf<Role?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedTipoPropiedad by remember { mutableStateOf("") }
    var selectedResidencia by remember { mutableStateOf<Residence?>(null) }
    var expandedRol by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedResidencia by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val residencesState by residencesViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        residencesViewModel.loadAvailableResidences()
    }
    val roles = listOf(
                Role(1,"Administrador"),
                Role(2, "Residente")
            )

    LaunchedEffect(state.success) {
        if (state.success) {
            showSuccessDialog = true
        }
    }


    val tiposPropiedad = listOf(stringResource(R.string.casa), stringResource(R.string.apartamento), stringResource(R.string.villa),
        stringResource(R.string.terreno), stringResource(R.string.local))

    val residenciasFiltradas = residencesState.residences.filter {
        it.type == selectedTipoPropiedad
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nuevo_usuario),
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Panel",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.processIntent(UsersIntent.NameChanged(it)) },
                label = { Text(text = stringResource(R.string.nombre)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = {
                    emailFormat = isValidEmail(it)
                    viewModel.processIntent(UsersIntent.EmailChanged(it))
                },
                isError = !emailFormat,
                label = { Text(text = stringResource(R.string.correo_electronico)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                placeholder = { Text(text = "example@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            OutlinedTextField(
                value = TextFieldValue(
                    text = state.idCard,
                    selection = TextRange(state.idCard.length)
                ),
                onValueChange = { newValue ->
                    val digits = newValue.text.replace(Regex("[^0-9]"), "")
                    val limited = if (digits.length > 11) digits.substring(0, 11) else digits
                    val formatted = formatIdCard(limited)
                    idCardFormat = isValidIdCard(formatted)
                    viewModel.processIntent(UsersIntent.IdCardChanged(formatted))
                },
                label = { Text(text = stringResource(R.string.cedula)) },
                placeholder = { Text("000-0000000-0") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null
                    )
                },
                isError = !idCardFormat,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true
            )

            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    validPassword = it.length >= 8
                    viewModel.processIntent(UsersIntent.PasswordChanged(it))
                },
                label = { Text(text = stringResource(R.string.contrase_a)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )
                },
                isError = !validPassword,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        if (passwordVisible) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                        }
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (!validPassword){
                Text(stringResource(R.string.la_contrase_a_debe_contener_al_menos_8_caracteres), color = Color.Red)
            }


            ExposedDropdownMenuBox(
                expanded = expandedRol,
                onExpandedChange = { expandedRol = it }
            ) {
                OutlinedTextField(
                    value = selectedRol?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedRol,
                    onDismissRequest = { expandedRol = false }
                ) {
                    roles.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.name) },
                            onClick = {
                                selectedRol = rol
                                expandedRol = false
                                viewModel.processIntent(UsersIntent.RoleChanged(rol.id))
                                if (rol.name != "Residente") {
                                    selectedTipoPropiedad = ""
                                    selectedResidencia = null
                                }
                            }
                        )
                    }
                }
            }


            AnimatedVisibility(visible = selectedRol?.name == "Residente") {
                // contenido visible solo si el rol es Residente
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dropdown Tipo de Propiedad
                    ExposedDropdownMenuBox(
                        expanded = expandedTipo,
                        onExpandedChange = { expandedTipo = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTipoPropiedad,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.tipo_de_propiedad)) },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
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
                                        selectedTipoPropiedad = tipo
                                        selectedResidencia = null // Reset residencia al cambiar tipo
                                        expandedTipo = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown Residencia (solo visible si hay tipo seleccionado)
                    if (selectedTipoPropiedad.isNotBlank()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedResidencia,
                            onExpandedChange = { expandedResidencia = it }
                        ) {
                            OutlinedTextField(
                                value = selectedResidencia?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.residencia)) },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedResidencia) },
                                placeholder = { Text(stringResource(R.string.selecciona_una_residencia)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedResidencia,
                                onDismissRequest = { expandedResidencia = false }
                            ) {
                                if (residenciasFiltradas.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.no_hay_residencias_disponibles)) },
                                        onClick = {},
                                        enabled = false
                                    )
                                } else {
                                    residenciasFiltradas.forEach { residencia ->
                                        DropdownMenuItem(
                                            text = { Text(residencia.name) },
                                            onClick = {
                                                viewModel.processIntent(UsersIntent.ResidenceChanged(residencia.id))
                                                selectedResidencia = residencia
                                                expandedResidencia = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                    title = { Text(stringResource(R.string.usuario_creado)) },
                    text = {
                        Text(
                            stringResource(R.string.el_usuario_ha_sido_creada_exitosamente)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showSuccessDialog = false
                            goBack()

                        }) {
                            Text(stringResource(R.string.aceptar))
                        }
                    }
                )
            }

            if (state.errorMessage != null){
                Text(stringResource(state.errorMessage!!.toInt()), color = Color.Red)
            }
            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = { viewModel.processIntent(UsersIntent.CreateUser) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.name.isNotBlank() &&
                        !state.isLoading &&
                        state.email.isNotBlank() &&
                        emailFormat &&
                        state.idCard.isNotBlank() &&
                        idCardFormat &&
                        state.password.isNotBlank() &&
                        validPassword &&
                        selectedRol != null &&
                        (selectedRol!!.name != stringResource(R.string.residente) || selectedResidencia != null)

            ) {
                if (!state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.guardar_usuario), style = MaterialTheme.typography.titleMedium)
                    }
                } else {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        )
                }
            }
        }
    }
}




