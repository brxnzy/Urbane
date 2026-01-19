package com.example.urbane.ui.admin.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.settings.view.components.LogoutButton
import com.example.urbane.ui.admin.settings.view.components.ProfileSection
import com.example.urbane.ui.admin.settings.view.components.SectionHeader
import com.example.urbane.ui.admin.settings.view.components.SettingItem
import com.example.urbane.ui.auth.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sessionManager: SessionManager,
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)

    ) {
        ProfileSection(user)

        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(stringResource(R.string.administraci_n))
        SettingItem(
            icon = Icons.Default.Apartment,
            title = stringResource(R.string.gestion_de_residenciales),
            subtitle = stringResource(R.string.gestiona_tus_residenciales_disponibles),
            onClick = { navController.navigate(Routes.ADMIN_SETTINGS_RESIDENTIALS) }
        )
        SettingItem(
            icon = Icons.Default.Poll,
            title = "Encuestas",
            subtitle = "Crea y administra  encuestas para la toma de decisiones del residencial.",
            onClick = { navController.navigate(Routes.ADMIN_SETTINGS_SURVEYS) }
        )

        SectionHeader("Gestion de catalogos")
        SettingItem(
            icon = Icons.Default.Build,
            title = "Servicios",
            subtitle = "Gestiona los servicios añadidos a los contratos",
            onClick = { /* TODO */ }
        )

        SettingItem(
            icon = Icons.Default.House,
            title = "Residencias",
            subtitle = "Gestiona las categorias de las residencias disponibles",
            onClick = { /* TODO */ }
        )

        SettingItem(
            icon = Icons.Default.Warning,
            title = "Incidencias",
            subtitle = "Gestiona las categorias de las incidencias",
            onClick = { /* TODO */ }
        )
        SectionHeader("Auditoria")
        SettingItem(
            icon = Icons.Default.Warning,
            title = "Auditoria",
            subtitle = "Registro de acciones y cambios realizados por los usuarios",
            onClick = { navController.navigate(Routes.ADMIN_SETTINGS_LOGS) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        LogoutButton(loginViewModel, navController)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Versión 2.24.1",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}



