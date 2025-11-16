package com.example.urbane.ui.auth.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.remote.supabase
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.auth.model.CurrentUser
import com.example.urbane.ui.auth.model.LoginIntent
import com.example.urbane.ui.auth.model.LoginState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser = _currentUser.asStateFlow()


    @RequiresApi(Build.VERSION_CODES.P)
    fun processIntent(intent: LoginIntent) {
        when (intent) {

            is LoginIntent.EmailChanged -> {
                _state.update { it.copy(email = intent.email) }
            }

            is LoginIntent.PasswordChanged -> {
                _state.update { it.copy(password = intent.password) }
            }

            is LoginIntent.Submit -> {
                handleSubmit()
            }

            is LoginIntent.ClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }

            LoginIntent.Logout -> {
                onLogoutClicked {  }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleSubmit() {
        viewModelScope.launch {
            try {
                Log.d("LoginVM", "Iniciando login...")
                _state.update { it.copy(isLoading = true, errorMessage = null) }

                val result = supabase.auth.signInWith(Email) {
                    email = state.value.email
                    password = state.value.password
                }
                val session = supabase.auth.currentSessionOrNull()
                if (session == null) throw Exception("No se pudo obtener la sesión")

                val userId = session.user?.id
                if (userId == null) throw Exception("No se pudo obtener userId")

                val email = session.user!!.email ?: state.value.email
                val roleId = UserRepository(sessionManager).getUserRole(userId)
                val userData = UserRepository(sessionManager).getCurrentUser(userId,email)
                Log.d("LoginVM","data del usuario $userData")

                val currentUser = CurrentUser(
                    userId = userId,
                    email = email,
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    roleId = roleId.toString(),
                    userData

                )
                _currentUser.update {currentUser}
                Log.d("LoginVM", "CurrentUser creado: $currentUser")

                sessionManager.saveSession(currentUser)
                Log.d("LoginVM", "Sesión guardada en SessionManager")

                _state.update { it.copy(isLoading = false, success = true, errorMessage = null) }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        R.string.credenciales_invalidas_o_cuenta_inexistente

                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains(
                                "No address associated with hostname",
                                ignoreCase = true
                            ) == true ->
                        R.string.sin_conexi_n_a_internet_verifica_tu_red_e_int_ntalo_de_nuevo

                    else -> e.message ?: R.string.error_desconocido_al_registrar_usuario
                }
                _state.update {
                    it.copy(
                        errorMessage = msg.toString(),
                        isLoading = false

                        )
                }
            }
        }
    }


    fun checkSession(onRoleFound: (String) -> Unit) {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { currentUser ->
                if (currentUser != null) {
                    onRoleFound(currentUser.roleId)
                }
            }
        }
    }

    suspend fun performLogout() {
            try {
                supabase.auth.signOut()
                sessionManager.clearSession()
                _state.update { it.copy(success = true,  ) }
                _currentUser.value = null
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error logout: ${e.message}")

        }
    }




    fun onLogoutClicked(toLogin: () -> Unit) {
        viewModelScope.launch {
        performLogout()
        toLogin()
        }
    }



}


