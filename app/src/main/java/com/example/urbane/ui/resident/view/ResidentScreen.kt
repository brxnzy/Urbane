package com.example.urbane.ui.resident.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun ResidentScreen(
    sessionManager: SessionManager,
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(innerNavController) }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                ResidentHomeContent(sessionManager, loginViewModel, navController)
            }

            composable("pagos") {
                PagosScreen(innerNavController)  // ← Vuelve al nombre original
            }

            composable("pagar_ahora/{concepto}/{monto}/{fecha}") { backStackEntry ->
                val concepto = backStackEntry.arguments?.getString("concepto") ?: ""
                val monto = backStackEntry.arguments?.getString("monto")?.toDoubleOrNull() ?: 0.0
                val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
                PagarAhoraScreen(  // ← Vuelve al nombre original
                    navController = innerNavController,
                    concepto = concepto,
                    monto = monto,
                    fecha = fecha
                )
            }

            composable("incidencias") {
                IncidenciasScreen()
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
        BottomNavItem("home", "Inicio", Icons.Filled.Home),
        BottomNavItem("pagos", "Pagos", Icons.Filled.Payment),
        BottomNavItem("incidencias", "Incidencias", Icons.Filled.Warning),
        BottomNavItem("mensajes", "Mensajes", Icons.Filled.Email),
        BottomNavItem("perfil", "Perfil", Icons.Filled.AccountCircle)
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