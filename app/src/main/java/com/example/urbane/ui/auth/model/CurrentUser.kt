package com.example.urbane.ui.auth.model

data class CurrentUser(
    val userId: String = "",
    val email: String = "",
    val accessToken: String = "",
    val refreshToken: String ="",
    val role: String=""
)
