package com.example.urbane.ui.admin.users.viewmodel
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.users.model.DetailSuccess
import com.example.urbane.ui.admin.users.model.UserDetailState
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
import com.example.urbane.ui.auth.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class UsersDetailViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(UserDetailState())
    val state = _state.asStateFlow()
    val userRepository = UserRepository(sessionManager)

    fun processIntent(intent: UsersDetailIntent) {
        when (intent) {
            is UsersDetailIntent.DisableUser -> disableUser()
            is UsersDetailIntent.EnableUser-> enableUser()
            is UsersDetailIntent.EditUser-> editUserRole(intent.newRoleId ,intent.residenceId)

            else -> {}

        }
    }


    fun loadUser(id: String){
        _state.update { it.copy(isLoading = true, user = null, errorMessage = null) }
        viewModelScope.launch {
//            if (_state.value.user != null) return@launch
            try {
                _state.update { it.copy(isLoading = true) }

                val user = userRepository.getUserById(id)
                Log.d("UsersVM", "usuarios disponibles activos $user")

                _state.update {
                    it.copy(isLoading = false, user = user , errorMessage = null)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun disableUser() {
        viewModelScope.launch {
            val id = _state.value.user?.id ?: return@launch

            try {
                Log.d("UsersVM", "Tratando de deshabilitar al usuario")

                _state.update { it.copy(isLoading = true, success = null, errorMessage = null) }

                userRepository.disableUser(id)

                _state.update { it.copy(isLoading = false, success = DetailSuccess.UserDisabled) }
                loadUser(id)


            } catch (e: Exception) {
                Log.e("UsersVM", "Error al deshabilitar usuario", e)
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun enableUser() {
        viewModelScope.launch {
            val id = _state.value.user?.id ?: return@launch
            try {
                Log.d("UsersVM", "Tratando de habilitar al usuario")

                _state.update { it.copy(isLoading = true, success = null, errorMessage = null) }

                val residenceId = _state.value.residenceId
                Log.d("UsersVM","ID DE LA RESIDENCIA $residenceId")
                val result = userRepository.enableUser(id, residenceId)

                if (result) {
                    Log.d("UsersVM", "Usuario habilitado correctamente")
                    _state.update { it.copy(isLoading = false, success = DetailSuccess.UserEnabled) }
                    loadUser(id)
                } else {
                    Log.d("UsersVM", "No se pudo habilitar al usuario")
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to enable user") }
                }

            } catch (e: Exception) {
                Log.e("UsersVM", "Error al habilitar usuario", e)
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }


    private fun editUserRole(newRoleId: Int, residenceId: String?) {
        viewModelScope.launch {
            val id = _state.value.user?.id ?: return@launch
            Log.d("UsersVM", "Editando rol: userId=${id}, newRoleId=$newRoleId, residenceId=$residenceId")

            try {
            _state.update { it.copy(isLoading = true, success = null, errorMessage = null) }

                val result = userRepository.updateUserRole(
                    userId = id,
                    newRoleId = newRoleId,
                    residenceId = residenceId?.toIntOrNull()
                )

                if (result) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = DetailSuccess.UserEdited
                    )
                    loadUser(id)

                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No se pudo actualizar el rol del usuario"
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun resetSuccess() {
        _state.update{it.copy(success = null )}
    }

    fun setResidenceId(id: Int?) {
        _state.update { it.copy(residenceId = id) }
    }






}

