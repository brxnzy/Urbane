package com.example.urbane.ui.admin.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun SettingsScreen(sessionManager: SessionManager, navController: NavController, loginViewModel: LoginViewModel) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value
    var notificationsEnabled by remember { mutableStateOf(true) }
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
                onClick = {navController.navigate(Routes.ADMIN_SETTINGS_RESIDENTIALS)}
            )
            SettingItem(
                icon = Icons.Default.Poll,
                title = "Encuestas",
                subtitle = "Crea y administra  encuestas para la toma de decisiones del residencial.",
                onClick = { navController.navigate(Routes.ADMIN_SETTINGS_SURVEYS) }
            )

//            SectionHeader("Preferencias")
//            SettingItemWithSwitch(
//                icon = Icons.Default.Notifications,
//                title = "Notificaciones",
//                subtitle = "Mensajes, grupos y tonos de llamada",
//                checked = notificationsEnabled,
//                onCheckedChange = { notificationsEnabled = it }
//            )
//            SettingItem(
//                icon = Icons.Default.Palette,
//                title = "Tema",
//                subtitle = "Claro, oscuro, automático",
//                onClick = { /* TODO */ }
//            )a
            SectionHeader("Finanzas")
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Monto base de contratos",
                subtitle = "Configura el valor base aplicado al crear nuevos pagos",
                onClick = { /* TODO */ }
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

@Composable
fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF000000)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF25D366),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFBDBDBD)
            )
        )
    }

    Divider(
        color = Color(0xFFE0E0E0),
        modifier = Modifier.padding(start = 72.dp)
    )
}

