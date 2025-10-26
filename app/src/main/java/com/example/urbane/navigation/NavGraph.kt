package com.example.urbane.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.Splash
import com.example.urbane.ui.admin.AdminMainScaffold
import com.example.urbane.ui.auth.view.LoginScreen
import com.example.urbane.ui.auth.view.RegisterScreen
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.auth.viewmodel.RegisterViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainNavigation(navController: NavHostController, modifier: Modifier) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            Splash(sessionManager = sessionManager) { role ->
                when (role) {
                    "1" -> navController.navigate(Routes.ADMIN_USERS) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                    "2" -> navController.navigate(Routes.RESIDENT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                    else -> navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.REGISTER) {
            val registerViewModel = RegisterViewModel()
            RegisterScreen(
                registerViewModel,
                modifier = modifier,
                toLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            val loginViewModel = LoginViewModel(sessionManager)
            LoginScreen(
                loginViewModel,
                sessionManager,
                modifier = modifier,
                toRegister = { navController.navigate(Routes.REGISTER) },
                navigateByRole = { roleId ->
                    when (roleId) {
                        "1" -> navController.navigate(Routes.ADMIN_USERS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        "2" -> navController.navigate(Routes.RESIDENT) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        else -> navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ADMIN_USERS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_USERS
            )
        }

        composable(Routes.ADMIN_RESIDENCES) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_RESIDENCES
            )
        }

        composable(Routes.ADMIN_PAYMENTS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_PAYMENTS
            )
        }
    }
}


