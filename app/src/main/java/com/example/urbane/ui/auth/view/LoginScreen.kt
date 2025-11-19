package com.example.urbane.ui.auth.view


import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.auth.model.LoginIntent
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.compose.runtime.livedata.observeAsState
import com.example.urbane.navigation.Routes


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LoginScreen(viewModel: LoginViewModel,sessionManager: SessionManager,navController: NavController, modifier: Modifier, toRegister:()-> Unit, navigateByRole:(String?)-> Unit) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var emailEmpty by remember { mutableStateOf(false) }
    var passwordEmpty by remember { mutableStateOf(false) }
    var triedSubmit by remember { mutableStateOf(false) }
    val currentUser by sessionManager.sessionFlow.collectAsState(initial = null)
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (state.success) {
                navigateByRole(user.roleId)
            }
        }
    }

    if (state.disabled) {
        LaunchedEffect(Unit) {
            viewModel.reset()
            navController.navigate(Routes.DISABLED) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    val successMsg = navController
        .previousBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("success_msg")
        ?.observeAsState()

    successMsg?.value?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("success_msg")
        }
    }



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Inicia Sesión",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Start
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = {
                    viewModel.processIntent(LoginIntent.EmailChanged(it))
                    if (triedSubmit) emailEmpty = it.isBlank()
                },
                label = { Text(stringResource(R.string.correo_electronico)) },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                placeholder = { Text("example@gmail.com") },
                modifier = Modifier.fillMaxWidth(0.75f),
                singleLine = true,
                isError = emailEmpty,
                colors = OutlinedTextFieldDefaults.colors(
                    errorLabelColor = Color.Red,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = {
                    viewModel.processIntent(LoginIntent.PasswordChanged(it))
                    if (triedSubmit) passwordEmpty = it.isBlank()
                },
                label = { Text(stringResource(R.string.contrase_a)) },
                modifier = Modifier.fillMaxWidth(0.75f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                isError = passwordEmpty,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    errorLabelColor = Color.Red,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Button(
                onClick = {
                    triedSubmit = true
                    emailEmpty = state.email.isBlank()
                    passwordEmpty = state.password.isBlank()

                    if (!emailEmpty && !passwordEmpty) {
                        viewModel.processIntent(LoginIntent.Submit)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.75f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.loguearse),
                    fontSize = 17.sp,
                    color = Color.White,
                    modifier = Modifier.padding(5.dp)
                )
            }

            if (state.isLoading){
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp)
                )
            }

//                if (state.errorMessage != null) {
//                    Text(stringResource(state.errorMessage!!.toInt()), color = Color.Red)
//                }



            Text("No tienes una cuenta?",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable{
                    toRegister()
                })
        }

    }
}









