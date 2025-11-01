package com.example.urbane.ui.auth.model

import android.content.Context
import android.net.Uri

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val idCard: String = "",
    val password: String = "",
    val residentialName: String = "",
    val residentialAddress: String = "",
    val residentialPhone: String = "",
    val logoUrl: Uri? =  null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false

)

sealed class RegisterIntent {
    data class NameChanged(val name: String) : RegisterIntent()
    data class EmailChanged(val email: String) : RegisterIntent()
    data class IdCardChanged(val idCard: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class ResidentialNameChanged(val residentialName: String) : RegisterIntent()
    data class ResidentialAddressChanged(val residentialAddress: String) : RegisterIntent()
    data class ResidentialPhoneChanged(val residentialPhone: String) : RegisterIntent()
    data class LogoChanged(val logoUrl: Uri?) : RegisterIntent()
    data class Submit(val context: Context) : RegisterIntent()
    data object ClearError : RegisterIntent()
}


