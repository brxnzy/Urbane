package com.example.urbane.ui.admin.users.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.users.model.UserDetailState
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
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

            else -> {}

        }
    }


    fun loadUser(id: String){
        _state.update { it.copy(isLoading = true, user = null, errorMessage = null) }
        viewModelScope.launch {
            if (_state.value.user != null) return@launch
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

                _state.update { it.copy(isLoading = true, success = false, errorMessage = null) }

                val result = userRepository.disableUser(id)

                _state.update { it.copy(isLoading = false, success = true) }

            } catch (e: Exception) {
                Log.e("UsersVM", "Error al deshabilitar usuario", e)
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }






}

