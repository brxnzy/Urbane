package com.example.urbane.ui.auth.model

data class LoginState(
val email : String = "",
val password : String = "",
val isLoading : Boolean = false,
val success : Boolean = false,
val errorMessage: String? = null,
)

sealed class LoginIntent{
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data object Submit : LoginIntent()
    data object ClearError: LoginIntent()
}

