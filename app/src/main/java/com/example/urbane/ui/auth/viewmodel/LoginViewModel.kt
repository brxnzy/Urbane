package com.example.urbane.ui.auth.viewmodel

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.R
import com.example.urbane.data.remote.supabase
import com.example.urbane.ui.auth.model.LoginIntent
import com.example.urbane.ui.auth.model.LoginState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

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
        }
    }
    private fun handleSubmit(){
        viewModelScope.launch {
            try {
                _state.update{
                    it.copy(isLoading = true)
                }

                val user = supabase.auth.signInWith(Email) {
                    email = state.value.email
                    password = state.value.password
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = true,
                        errorMessage = null
                    )}

            } catch (e: Exception){
                val msg = when {
                    e.message?.contains("Invalid login credentials", ignoreCase = true) == true ->
                        R.string.credenciales_invalidas_o_cuenta_inexistente

                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                       R.string.sin_conexi_n_a_internet_verifica_tu_red_e_int_ntalo_de_nuevo

                    else -> e.message ?: R.string.error_desconocido_al_registrar_usuario
                }
                _state.update{
                    it.copy(
                        errorMessage = msg.toString(),

                    )
                }
        }


        }

    }


}


