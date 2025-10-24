package com.example.urbane.ui.auth.viewmodel

import com.example.urbane.data.local.SessionManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.auth.model.CurrentUser
import com.example.urbane.ui.auth.model.RegisterIntent
import com.example.urbane.ui.auth.model.RegisterState

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.UnknownRestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject


class RegisterViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow(CurrentUser())
    val currentUser: StateFlow<CurrentUser> = _currentUser


    fun processIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> {
                _state.update { it.copy(name = intent.name) }
            }

            is RegisterIntent.EmailChanged -> {
                _state.update { it.copy(email = intent.email) }
            }

            is RegisterIntent.IdCardChanged -> {
                _state.update { it.copy(idCard = intent.idCard) }
            }

            is RegisterIntent.PasswordChanged -> {
                _state.update { it.copy(password = intent.password) }
            }

            is RegisterIntent.ResidentialNameChanged -> {
                _state.update { it.copy(residentialName = intent.residentialName) }
            }

            is RegisterIntent.ResidentialAddressChanged -> {
                _state.update { it.copy(residentialAddress = intent.residentialAddress) }
            }

            is RegisterIntent.ResidentialPhoneChanged -> {
                _state.update { it.copy(residentialPhone = intent.residentialPhone) }
            }

            is RegisterIntent.Submit -> {
                handleSubmit()
            }

            is RegisterIntent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }


        }
    }

    private fun handleSubmit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d("Register", "=== Inicio del registro ===")

                // 1️⃣ Registro en Supabase
                val result = supabase.auth.signUpWith(Email) {
                    email = state.value.email
                    password = state.value.password
                    data = buildJsonObject {
                        put("name", JsonPrimitive(state.value.name))
                        put("idCard", JsonPrimitive(state.value.idCard))
                        put("residentialName", JsonPrimitive(state.value.residentialName))
                        put("residentialAddress", JsonPrimitive(state.value.residentialAddress))
                        put("residentialPhone", JsonPrimitive(state.value.residentialPhone))
                    }
                }
                Log.d("Register", "Usuario registrado correctamente: $result")


                val session = supabase.auth.currentSessionOrNull()
                if (session == null) throw Exception("No se pudo obtener la sesión después del registro")
                Log.d("Register", "Sesión obtenida: $session")

                val userId = session.user?.id
                Log.d("Register", "UserId: $userId")




                // 4️⃣ Construir CurrentUser
                val currentUser = CurrentUser(
                    userId = userId.toString(),
                    email = session.user?.email ?: "",
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    role = "admin"
                )
                Log.d("Registerr", "CurrentUser construido: $currentUser")

                sessionManager.saveSession(currentUser)
                Log.d("Registerr", "Sesión guardada en DataStore")


                _state.update { it.copy(isLoading = false, success = true) }
                Log.d("Registerr", "Registro completado y estado actualizado")

            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("User already registered", ignoreCase = true) == true ->
                        "Ya existe un usuario registrado con ese correo electrónico"

                    e is UnknownRestException ->
                        "La cédula ingresada ya está registrada o hay un dato duplicado"

                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                        "Sin conexión a internet. Verifica tu red e inténtalo de nuevo"

                    else -> e.message ?: "Error desconocido al registrar usuario"
                }

                Log.e("Registerr", "Error en el registro: $msg", e)

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMessage = msg
                    )
                }
            }
        }
    }
}




