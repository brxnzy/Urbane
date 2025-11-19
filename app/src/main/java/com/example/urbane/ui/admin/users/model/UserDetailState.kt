package com.example.urbane.ui.admin.users.model
import com.example.urbane.data.model.User

data class UserDetailState(
    val isLoading: Boolean = false,
    val success: DetailSuccess? = null,
    val errorMessage: String? = null,
    val user: User? = null,
    val residenceId :Int? = null
)


sealed class UsersDetailIntent {
    data object DisableUser : UsersDetailIntent()
    data object EnableUser: UsersDetailIntent()


}


sealed class DetailSuccess {
    object UserEdited : DetailSuccess()
    object UserEnabled : DetailSuccess()
    object UserDisabled : DetailSuccess()
}
