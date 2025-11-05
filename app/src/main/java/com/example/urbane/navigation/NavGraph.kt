package com.example.urbane.navigation

import AddResidenceScreen
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.Splash
import com.example.urbane.ui.admin.AdminMainScaffold

import com.example.urbane.ui.admin.users.view.AddUserScreen
import com.example.urbane.ui.admin.viewmodel.MainViewModel
import com.example.urbane.ui.auth.view.LoginScreen
import com.example.urbane.ui.auth.view.RegisterScreen
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.auth.viewmodel.RegisterViewModel
import com.example.urbane.ui.resident.view.ResidentScreen

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("ViewModelConstructorInComposable", "ComposableDestinationInComposeScope")
@Composable
fun MainNavigation(navController: NavHostController, modifier: Modifier) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val loginViewModel = LoginViewModel(sessionManager)


    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            Splash(sessionManager = sessionManager) { role ->
                when (role) {
                    "1" -> navController.navigate(Routes.ADMIN) {
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
                navController,
                modifier = modifier,
                toLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                loginViewModel,
                sessionManager,
                navController,
                modifier = modifier,
                toRegister = { navController.navigate(Routes.REGISTER) },
                navigateByRole = { roleId ->
                    when (roleId) {
                        "1" -> navController.navigate(Routes.ADMIN) {
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
                currentRoute = Routes.ADMIN_USERS,
                loginViewModel,
                sessionManager

            )
        }

        composable(Routes.ADMIN_RESIDENCES) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_RESIDENCES,
                loginViewModel,
                sessionManager

            )

        }

        composable(Routes.ADMIN) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN,
                loginViewModel,
                sessionManager
            )

        }


        composable(Routes.ADMIN_PAYMENTS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_PAYMENTS,
                loginViewModel,
                sessionManager
            )
        }

        composable(Routes.ADMIN_USERS_ADD) {
            AddUserScreen(){
                navController.navigate(Routes.ADMIN_USERS)
            }
        }
        composable(Routes.ADMIN_RESIDENCES_ADD) {
            AddResidenceScreen(){
                navController.navigate(Routes.ADMIN_RESIDENCES)
            }
        }

        composable(Routes.RESIDENT){
            ResidentScreen(sessionManager, loginViewModel, navController)
        }
    }
}














