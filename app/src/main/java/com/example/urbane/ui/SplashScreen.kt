package com.example.urbane.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.theme.DarkGray
import kotlinx.coroutines.delay
@Composable
fun Splash(
    sessionManager: SessionManager,
    onRoleFound: (String?) -> Unit
) {

    val sessionState = sessionManager.sessionFlow.collectAsState(initial = null)
    val session = sessionState.value

    LaunchedEffect(session) {
        delay(1500)
        if (session != null) {
            onRoleFound(session.roleId)
        } else {
            onRoleFound(null)
        }
    }

    SplashScreen()
}



@Preview(showSystemUi = true)
@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.size(200.dp)
        )

    }
}