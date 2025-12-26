package com.example.urbane.ui.admin.contracts.model

import com.example.urbane.data.model.Contract
import com.example.urbane.data.model.ContractService
import com.example.urbane.data.model.Service

data class ContractsDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: ContractsDetailSuccess? = null,
    val contract: Contract? = null,
    val contractServices: List<ContractService> = emptyList(),
    val availableServices: List<Service> = emptyList()
)

sealed class ContractsDetailIntent {
    data class UpdateContract(
        val contractId: Int,
        val conditions: String,
        val amount: Double
    ) : ContractsDetailIntent()

    data class AddService(val contractId: Int, val serviceId: Int) : ContractsDetailIntent()
    data class RemoveService(val contractId: Int, val contractServiceId: Int) : ContractsDetailIntent()
    object ClearMessages : ContractsDetailIntent()
}

sealed class ContractsDetailSuccess {
    object UpdateContract : ContractsDetailSuccess()
    object AddService : ContractsDetailSuccess()
    object RemoveService : ContractsDetailSuccess()
}