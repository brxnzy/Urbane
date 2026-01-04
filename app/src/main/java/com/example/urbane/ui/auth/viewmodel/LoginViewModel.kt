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
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LoginViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    val userRepository = UserRepository(sessionManager)

    private var tempUserId: String? = null
    private var tempEmail: String? = null
    private var tempAccessToken: String? = null
    private var tempRefreshToken: String? = null
    private var tempRoleId: String? = null


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
                onLogoutClicked { }
            }
            // ✅ NUEVO
            is LoginIntent.ResidentialSelected -> {
                handleResidentialSelection(intent.residentialId)
            }

            LoginIntent.ShowResidentialSelector -> {
                loadAvailableResidentials()
            }

            LoginIntent.DismissResidentialSelector -> {
                dismissResidentialSelector()
            }

        }
    }

    // ✅ NUEVA función
    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleResidentialSelection(residentialId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, showResidentialSelector = false) }

                // Verificar si es durante login o cambio de sesión activa
                if (tempUserId != null) {
                    // ✅ Durante el login inicial
                    val userId = tempUserId ?: throw Exception("No user data")
                    val email = tempEmail ?: throw Exception("No email")

                    val userData = userRepository.getCurrentUserForResidential(userId, email, residentialId)

                    val currentUser = CurrentUser(
                        userId = userId,
                        email = email,
                        accessToken = tempAccessToken!!,
                        refreshToken = tempRefreshToken!!,
                        roleId = tempRoleId!!,
                        userData
                    )

                    sessionManager.saveSession(currentUser)
                    saveFcmToken(userId, residentialId, tempRoleId!!)

                    clearTempData()

                    _state.update { it.copy(isLoading = false, success = true) }
                } else {
                    // ✅ Cambio de residencial en sesión activa
                    val currentUser = sessionManager.sessionFlow.first()
                    if (currentUser == null) throw Exception("No hay sesión activa")

                    val userData = userRepository.getCurrentUserForResidential(
                        currentUser.userId,
                        currentUser.email,
                        residentialId
                    )

                    val updatedUser = currentUser.copy(userData = userData)
                    sessionManager.saveSession(updatedUser)

                    // Actualizar FCM token con nuevo residencial
                    saveFcmToken(currentUser.userId, residentialId, currentUser.roleId)

                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("LoginVM", "Error seleccionando residencial: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al seleccionar residencial"
                    )
                }
            }
        }
    }

    private fun clearTempData() {
        tempUserId = null
        tempEmail = null
        tempAccessToken = null
        tempRefreshToken = null
        tempRoleId = null
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
                Log.d("LoginVM", "$result")

                if (session == null) throw Exception("No se pudo obtener la sesión")

                val userId = session.user?.id
                if (userId == null) throw Exception("No se pudo obtener userId")

                val disabled = userRepository.isUserDisabled(userId)
                Log.d("LoginVM", "$disabled")
                if (disabled == true) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            disabled = true,
                            errorMessage = null
                        )
                    }
                    supabase.auth.signOut()
                    return@launch
                }

                val email = session.user!!.email ?: state.value.email
                val roleId = userRepository.getUserRole(userId)

// ✅ AGREGAR ESTO AQUÍ
                val residentials = userRepository.getUserResidentials(userId)

                if (residentials.isEmpty()) {
                    throw Exception("Usuario sin residencial asignado")
                }

// Si tiene más de 1 residencial, mostrar selector
                if (residentials.size > 1) {
                    // Guardar datos temporalmente
                    tempUserId = userId
                    tempEmail = email
                    tempAccessToken = session.accessToken
                    tempRefreshToken = session.refreshToken
                    tempRoleId = roleId.toString()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            showResidentialSelector = true,
                            availableResidentials = residentials
                        )
                    }
                    return@launch // Detener aquí y esperar selección
                }

// Si solo tiene 1 residencial, continuar normal
                val userData = userRepository.getCurrentUser(userId, email)
                Log.d("LoginVM", "data del usuario $userData")

// ... resto del código actual sin tocar                val userData = userRepository.getCurrentUser(userId,email)
                Log.d("LoginVM", "data del usuario $userData")

                val currentUser = CurrentUser(
                    userId = userId,
                    email = email,
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    roleId = roleId.toString(),
                    userData
                )

                Log.d("LoginVM", "CurrentUser creado: $currentUser")

                sessionManager.saveSession(currentUser)
                Log.d("LoginVM", "Sesión guardada en SessionManager")

                saveFcmToken(userId, userData!!.residential.id, roleId.toString())

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


    fun loadAvailableResidentials() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val currentUser = sessionManager.sessionFlow.first()
                if (currentUser == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                val residentials = userRepository.getUserResidentials(currentUser.userId)

                if (residentials.size > 1) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showResidentialSelector = true,
                            availableResidentials = residentials
                        )
                    }
                } else {
                    // Solo tiene 1 residencial, no mostrar selector
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("LoginVM", "Error cargando residenciales: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar residenciales"
                    )
                }
            }
        }


    }

    // ✅ NUEVA función para cerrar el dialog
    fun dismissResidentialSelector() {
        _state.update { it.copy(showResidentialSelector = false) }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun saveFcmToken(userId: String, residentialId: Int, roleId: String) {
        try {
            val token = Firebase.messaging.token.await()
            Log.d("LoginVM", "Token FCM obtenido: $token")

            val role = when (roleId) {
                "1" -> "admin"
                "2" -> "resident"
                else -> "resident"
            }

            // ✅ Usar buildJsonObject en lugar de mapOf
            val tokenData = buildJsonObject {
                put("user_id", userId)
                put("residential_id", residentialId)
                put("role", role)
                put("fcm_token", token)
                put("updated_at", Clock.System.now().toString())
            }

            supabase.from("user_tokens")
                .upsert(tokenData)

            Log.d("LoginVM", "Token FCM guardado exitosamente")
        } catch (e: Exception) {
            Log.e("LoginVM", "Error guardando token FCM: ${e.message}", e)
        }
    }

    suspend fun performLogout() {
        try {
            supabase.auth.signOut()
            sessionManager.clearSession()
            _state.update { it.copy(success = true) }
            _currentUser.value = null
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error logout: ${e.message}")

        }
    }

    fun reset() {
        _state.value = LoginState()
    }

    fun onLogoutClicked(toLogin: () -> Unit) {
        viewModelScope.launch {
            try {

                performLogout()
                toLogin()
            } catch (e: Exception) {
                Log.e("LOGOUT", e.toString())
            }
        }
    }


}


