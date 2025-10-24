package com.example.urbane.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                val user = supabase.auth.signInWith(Email) {
                    email = state.value.email
                    password = state.value.password
                }
            } catch (e: Exception){
                Log.e("Error en el login", "$e")
        }


        }

    }
}


