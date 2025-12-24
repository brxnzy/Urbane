package com.example.urbane.ui.admin.fines.model
import com.example.urbane.data.model.Fine
import com.example.urbane.data.model.Payment

data class FineDetailState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null,
    val fine: Fine? = null,
    val payment: Payment? = null
)
