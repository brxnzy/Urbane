package com.example.urbane.ui.admin.users.model

import com.example.urbane.data.model.Residence
import com.example.urbane.data.model.User

data class UserState(
    val name: String = "",
    val email: String = "",
    val idCard: String = "",
    val password: String = "",
    val roleId: Int = 0,
    val residenceId: Int? = null,
    val activeUsers: List<User> = emptyList(),
    val inactiveUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)


sealed class UsersIntent {
    data class NameChanged(val name: String) : UsersIntent()
    data class EmailChanged(val email: String) : UsersIntent()
    data class IdCardChanged(val idCard: String) : UsersIntent()
    data class PasswordChanged(val password: String) : UsersIntent()
    data class RoleChanged(val roleId: Int) : UsersIntent()
    data class ResidenceChanged(val residenceId: Int?) : UsersIntent()
    data object CreateUser : UsersIntent()
}

