package com.example.urbane.ui.auth.model

import com.example.urbane.data.model.UserResidentialRole
import kotlinx.serialization.Serializable


@Serializable
data class CurrentUser(
    val userId: String = "",
    val email: String = "",
    val accessToken: String = "",
    val refreshToken: String ="",
    val roleId: String = "",
    val userData: UserResidentialRole?
)
