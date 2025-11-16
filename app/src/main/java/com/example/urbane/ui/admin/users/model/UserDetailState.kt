package com.example.urbane.ui.admin.users.model
import com.example.urbane.data.model.User

data class UserDetailState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null
)


sealed class UsersDetailIntent {
    data object DisableUser : UsersDetailIntent()


}