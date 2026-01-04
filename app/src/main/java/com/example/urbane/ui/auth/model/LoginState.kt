package com.example.urbane.ui.auth.model

import com.example.urbane.data.model.Residential

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val disabled: Boolean = false,

    // ✅ Nuevos campos para selector de residencial
    val showResidentialSelector: Boolean = false,
    val availableResidentials: List<Residential> = emptyList(),
    val selectedResidentialId: Int? = null
)

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data object Submit : LoginIntent()
    data object ClearError : LoginIntent()
    data object Logout : LoginIntent()

    // ✅ Nuevo intent para seleccionar residencial
    data class ResidentialSelected(val residentialId: Int) : LoginIntent()
    data object ShowResidentialSelector : LoginIntent() // ✅ NUEVO
    data object DismissResidentialSelector : LoginIntent()
}