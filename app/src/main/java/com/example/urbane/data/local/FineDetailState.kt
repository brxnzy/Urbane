package com.example.urbane.data.local

import com.example.urbane.data.model.Fine


data class FineDetailState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val fine: Fine? = null
)
