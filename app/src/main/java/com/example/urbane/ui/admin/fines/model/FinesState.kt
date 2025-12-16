package com.example.urbane.ui.admin.fines.model

import com.example.urbane.data.model.Fine

data class FinesState(
    val isLoading: Boolean = false,
    val fines: List<Fine> = emptyList(),
    val error: String? = null
)
