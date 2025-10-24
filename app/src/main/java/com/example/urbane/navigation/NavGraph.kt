package com.example.urbane.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.Splash
import com.example.urbane.ui.auth.view.Login
import com.example.urbane.ui.auth.view.RegisterScreen
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.auth.viewmodel.RegisterViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainNavigation(navController: NavHostController, modifier: Modifier){
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ){
        composable(Routes.SPLASH) {
            Splash(
                modifier = Modifier.padding(0.dp),
                navController = navController
            ) {
                navController.navigate("login")
            }
        }
        composable(Routes.REGISTER){
            val context = LocalContext.current
            val sessionManager = SessionManager(context)
            val registerViewModel = RegisterViewModel(sessionManager)
            RegisterScreen(registerViewModel,modifier = modifier, toLogin = { navController.navigate("login")}) }
        composable(Routes.LOGIN){
            val loginViewModel = LoginViewModel()
            Login(loginViewModel, modifier = modifier, toRegister = { navController.navigate("register")})
     }
        }
    }

