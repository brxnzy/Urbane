package com.example.urbane.ui.admin.contracts.model
import com.example.urbane.data.model.Contract

data class ContractsState(
    val contracts: List<Contract> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
