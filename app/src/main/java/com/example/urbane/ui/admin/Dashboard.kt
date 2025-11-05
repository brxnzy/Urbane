package com.example.urbane.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.urbane.data.local.SessionManager


@Composable
fun Dashboard(sessionManager: SessionManager) {
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
}

