package com.example.urbane.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.twotone.Payments
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.urbane.R
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.users.UsersScreen
import com.example.urbane.ui.admin.residences.ResidencesScreen
import com.example.urbane.ui.admin.payments.PaymentsScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScaffold(
    navController: NavHostController,
    currentRoute: String
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.surface) // tu color para drawer en modo oscuro
            ) {
                DrawerContent(currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.ADMIN_USERS) { inclusive = false }
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text= when(currentRoute){
                            Routes.ADMIN_USERS -> stringResource(R.string.usuarios)
                            Routes.ADMIN_PAYMENTS -> stringResource(R.string.pagos)
                            Routes.ADMIN_RESIDENCES -> stringResource(R.string.residencias)
                            else -> "Panel Admin"
                        }, style = MaterialTheme.typography.displayMedium)


                            },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", modifier = Modifier.size(30.dp))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                // Aquí solo mostramos el contenido según la ruta actual
                when (currentRoute) {
                    Routes.ADMIN_USERS -> UsersScreen(modifier = Modifier.padding(16.dp))
                    Routes.ADMIN_RESIDENCES -> ResidencesScreen(modifier = Modifier.padding(16.dp))
                    Routes.ADMIN_PAYMENTS -> PaymentsScreen(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun DrawerContent(currentRoute: String,onDestinationClicked: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 35.dp)) {
        Text(
            "Menú",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))
        DrawerItem("Usuarios", Icons.Default.Person, Routes.ADMIN_USERS, currentRoute, onDestinationClicked)
        DrawerItem("Residencias", Icons.Default.House, Routes.ADMIN_RESIDENCES, currentRoute, onDestinationClicked)
        DrawerItem("Pagos", Icons.Default.Payments, Routes.ADMIN_PAYMENTS, currentRoute, onDestinationClicked)

    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    route: String,
    currentRoute: String,
    onClick: (String) -> Unit
) {
    val isSelected = route == currentRoute

    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else Color.Transparent

    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding(top = 4.dp, bottom = 4.dp, end = 10.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 35.dp,
            bottomEnd = 35.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(color = contentColor)
            )
        }
    }
}
