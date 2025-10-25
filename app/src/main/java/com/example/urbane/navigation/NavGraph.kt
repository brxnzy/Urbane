package com.example.urbane.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.Splash
import com.example.urbane.ui.SplashScreen
import com.example.urbane.ui.admin.Admin
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
                    "1" -> navController.navigate(Routes.ADMIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                    "2"-> navController.navigate(Routes.RESIDENT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }else -> navController.navigate(Routes.LOGIN){
                        popUpTo(Routes.SPLASH) {inclusive = true}
                }

                }
            }
        }
        composable(Routes.REGISTER) {

            val registerViewModel = RegisterViewModel(sessionManager)
            RegisterScreen(
                registerViewModel,
                modifier = modifier,
                toLogin = { navController.navigate("login") })
        }
        composable(Routes.LOGIN) {
            val loginViewModel = LoginViewModel(sessionManager)
            LoginScreen(
                loginViewModel,
                sessionManager ,
                modifier = modifier,
                toRegister = { navController.navigate("register") },
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
            )}


        composable(Routes.ADMIN) {
            val loginViewModel = LoginViewModel(sessionManager)
            Admin(loginViewModel){
                navController.navigate("login")
        }
            }
        }
    }


