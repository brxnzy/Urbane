package com.example.urbane.ui.admin.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.UserRepository
import com.example.urbane.ui.admin.users.model.UserState
import com.example.urbane.ui.admin.users.model.UsersIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class UsersViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state = _state.asStateFlow()

    fun processIntent(intent: UsersIntent) {
        when (intent) {
            is UsersIntent.NameChanged -> _state.update { it.copy(name = intent.name) }
            is UsersIntent.EmailChanged -> _state.update { it.copy(email = intent.email) }
            is UsersIntent.IdCardChanged -> _state.update { it.copy(idCard = intent.idCard) }
            is UsersIntent.PasswordChanged -> _state.update { it.copy(password = intent.password) }
            is UsersIntent.RoleChanged -> _state.update { it.copy(roleId = intent.roleId) }
            is UsersIntent.ResidenceChanged -> _state.update { it.copy(residenceId  = intent.residenceId) }
            UsersIntent.CreateUser -> createUser()
        }
    }

    private fun createUser() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val user = UserRepository(
                    sessionManager
                ).createUser(_state.value.name,_state.value.email,_state.value.idCard,_state.value.password,_state.value.roleId,_state.value.residenceId)

                _state.update { it.copy(isLoading = false, success = true) }


            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }

            }
        }
    }
}
