package com.example.urbane.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbane.ui.Splash
import com.example.urbane.ui.auth.view.RegisterScreen
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
                navController.navigate("register")
            }
        }
        composable(Routes.REGISTER){
            val registerViewModel = RegisterViewModel()
            RegisterScreen(registerViewModel,modifier = modifier) }
    }
}