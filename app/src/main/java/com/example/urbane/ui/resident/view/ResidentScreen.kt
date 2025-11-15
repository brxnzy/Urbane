package com.example.urbane.ui.resident.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.data.local.SessionManager
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.auth.viewmodel.LoginViewModel

@Composable
fun ResidentScreen(sessionManager: SessionManager, loginViewModel: LoginViewModel, navController: NavController){
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (user) {
            null -> CircularProgressIndicator()
            else -> Text("Hola, ${user.userData?.user?.name}")
        }
    }
    Button(
        onClick = {loginViewModel.onLogoutClicked { navController.navigate(Routes.LOGIN)
        {popUpTo(0) { inclusive = true }
            launchSingleTop = true } }},

        modifier = Modifier.fillMaxWidth().padding(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White

        ),
        shape =  RoundedCornerShape(
            topStart = 10.dp,
            bottomStart = 10.dp,
            topEnd = 10.dp,
            bottomEnd = 10.dp
        )
    ) {

        Row (modifier = Modifier.padding(vertical = 5.dp),horizontalArrangement = Arrangement.spacedBy(10.dp)

        ){

            Icon(Icons.Default.Logout, contentDescription = "Cerrar sesion")
            Text("Cerrar Sesion", style = MaterialTheme.typography.bodyMedium)

        }
    }



}

