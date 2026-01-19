package com.example.urbane.ui.resident.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.resident.viewmodel.PagosViewModel
import com.example.urbane.ui.resident.viewmodel.ResidentHomeContentViewModel

@Composable
fun ResidentScreen(
    sessionManager: SessionManager,
    loginViewModel: LoginViewModel,
    navController: NavController,
    pagosViewModel: PagosViewModel,
    residentHomeContentViewModel: ResidentHomeContentViewModel,

    ) {
    val innerNavController = rememberNavController()

    val userId by sessionManager.userIdFlow.collectAsState(initial = "")
    val residentialId by sessionManager.residentialIdFlow.collectAsState(initial = 0)

    Scaffold(
        bottomBar = { BottomNavBar(innerNavController) }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                ResidentHomeContent(sessionManager, residentHomeContentViewModel)
            }

            composable("pagos") {
                PagosScreen(innerNavController, pagosViewModel, sessionManager)
            }

            composable("pagar_ahora/{concepto}/{monto}/{fecha}") { backStackEntry ->
                val concepto = backStackEntry.arguments?.getString("concepto") ?: ""
                val monto = backStackEntry.arguments?.getString("monto")?.toDoubleOrNull() ?: 0.0
                val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
                PagarAhoraScreen(
                    navController = innerNavController,
                    concepto = concepto,
                    monto = monto,
                    fecha = fecha
                )
            }

            composable("incidencias") {
                IncidenciasScreen(
                    residentId = userId ?: "",
                    residentialId = residentialId
                )
            }

            composable("mensajes") {
                MensajesScreen()
            }

            composable("perfil") {
                PerfilScreen(sessionManager, loginViewModel, navController, innerNavController)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", "", Icons.Filled.Home),
        BottomNavItem("pagos", "", Icons.Filled.Payment),
        BottomNavItem("incidencias", "", Icons.Filled.Warning),
        BottomNavItem("mensajes", "", Icons.Filled.Email),
        BottomNavItem("perfil", "", Icons.Filled.AccountCircle)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)