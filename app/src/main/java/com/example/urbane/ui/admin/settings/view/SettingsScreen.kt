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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.urbane.data.local.SessionManager
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.auth.model.CurrentUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(sessionManager: SessionManager, navController: NavController) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value

    var notificationsEnabled by remember { mutableStateOf(true) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Profile Section
            ProfileSection(user)

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader("Preferencias")
            SettingItemWithSwitch(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = "Mensajes, grupos y tonos de llamada",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingItem(
                icon = Icons.Default.Palette,
                title = "Tema",
                subtitle = "Claro, oscuro, automático",
                onClick = { /* TODO */ }
            )

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
            LogoutButton()

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
fun ProfileSection(user: CurrentUser?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (user?.userData?.user?.photoUrl != null) {
            AsyncImage(
                model = user.userData.user.photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(76.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user?.userData?.user?.name ?: "",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF000000)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user?.userData?.user?.email ?: "",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }

    Divider(color = Color(0xFFE0E0E0))
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }

    Divider(
        color = Color(0xFFE0E0E0),
        modifier = Modifier.padding(start = 72.dp)
    )
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

@Composable
fun LogoutButton() {
    Button(
        onClick = { /* TODO */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cerrar sesión",
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}