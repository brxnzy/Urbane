package com.example.urbane.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.R
import com.example.urbane.data.remote.supabase
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


class RegisterViewModel() : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()




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

                Log.d("Result", "$result")

                supabase.auth.signOut()
                _state.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("User already registered", ignoreCase = true) == true ->
                      R.string.ya_existe_un_usuario_registrado_con_ese_correo_electr_nico

                    e is UnknownRestException ->
                        R.string.la_c_dula_ingresada_ya_est_registrada_o_hay_un_dato_duplicado

                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                            e.message?.contains("No address associated with hostname", ignoreCase = true) == true ->
                        R.string.sin_conexi_n_a_internet_verifica_tu_red_e_int_ntalo_de_nuevo

                    else -> e.message ?:  R.string.error_desconocido_al_registrar_usuario
                }

                Log.e("Registerr", "Error en el registro: $msg", e)

                _state.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        errorMessage = msg.toString()
                    )
                }
            }
        }
    }


}




