package com.example.urbane.ui.auth.view


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.R


@Composable
fun RegisterScreen(modifier: Modifier) {
    var currentStep by remember { mutableStateOf(0) }

    // Estados para los inputs - Paso 1
    var name by remember { mutableStateOf(value = "") }
    var idCard by remember { mutableStateOf(value = "") }
    var email by remember { mutableStateOf(value = "") }
    var password by remember { mutableStateOf(value = "") }
    var passwordVisible by remember { mutableStateOf(value = false) }

    var residentialName by remember { mutableStateOf(value = "") }
    var residentialAddress by remember { mutableStateOf(value = "") }
    var residentialPhone by remember{mutableStateOf(value="")}

    var error by remember { mutableStateOf(value = false) }

    val totalSteps = 2

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crea tu Cuenta",
                style = MaterialTheme.typography.displayLarge
                )
            Text(
                text = if (currentStep == 0) "1. Datos Personales" else "2. Datos del Residencial",
                style = MaterialTheme.typography.displayMedium,

            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.7f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i <= currentStep) MaterialTheme.colorScheme.primary
                                else Color(0xFFE0E0E0)
                            )
                    )
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        0 -> {
                            // Paso 1: Datos Personales
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = {
                                    Text(text = stringResource(R.string.nombre))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = {
                                    Text(text = stringResource(R.string.correo_electronico))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )

                            OutlinedTextField(
                                value = idCard,
                                onValueChange = { idCard = it },
                                label = {
                                    Text(text = stringResource(R.string.cedula))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = {
                                    Text(text = stringResource(R.string.contrase_a))
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Text(
                                            text = if (passwordVisible) stringResource(R.string.ocultar) else stringResource(
                                                R.string.mostrar
                                            )
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )
                        }

                        1 -> {
                            OutlinedTextField(
                                value = residentialName,
                                onValueChange = { residentialName = it },
                                label = {
                                    Text(text = "Nombre del Residencial")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )


                            OutlinedTextField(
                                value = residentialAddress,
                                onValueChange = { residentialAddress = it },
                                label = {
                                    Text(text = "Dirección")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )
                            OutlinedTextField(
                                value = residentialPhone
                                ,
                                onValueChange = { residentialPhone = it },
                                label = {
                                    Text(text = "Teléfono")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                                isError = error,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    errorLabelColor = Color.Red,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                )
                            )


                        }
                    }
                }
            }

            // Botones de navegación
            if (currentStep == 0) {
                // Solo botón Siguiente en paso 1
                Button(
                    onClick = { currentStep = 1 },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Siguiente",
                        color = Color.White,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { currentStep = 0 },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Volver",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    Button(
                        onClick = { /* Registrarse */ },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Aceptar",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }


            Text(
                text = "¿Ya tienes una cuenta?",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

