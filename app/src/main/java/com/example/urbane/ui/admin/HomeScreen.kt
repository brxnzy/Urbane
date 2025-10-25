package com.example.urbane.ui.admin

import android.R.attr.onClick
import android.widget.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.auth.viewmodel.LoginViewModel

@Composable
fun Admin(loginViewModel: LoginViewModel, toLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Bienvenido Admin...")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            loginViewModel.onLogoutClicked(toLogin)
        }) {
            Text("Cerrar sesión")
        }
    }
}
