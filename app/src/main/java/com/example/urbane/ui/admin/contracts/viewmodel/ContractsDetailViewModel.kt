package com.example.urbane.ui.admin.contracts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.repository.ContractsRepository
import com.example.urbane.data.repository.ServicesRepository
import com.example.urbane.ui.admin.contracts.model.ContractsDetailIntent
import com.example.urbane.ui.admin.contracts.model.ContractsDetailState
import com.example.urbane.ui.admin.contracts.model.ContractsDetailSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContractsDetailViewModel(val sessionManager: SessionManager) : ViewModel() {

    private val contractsRepository = ContractsRepository(sessionManager)
    private val servicesRepository = ServicesRepository(sessionManager)

    private val _state = MutableStateFlow(ContractsDetailState())
    val state: StateFlow<ContractsDetailState> = _state.asStateFlow()


    fun loadContract(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, success = null) }

            try {
                val contract = contractsRepository.getContractById(id)
                val contractServices = servicesRepository.getContractServices(id)
                val availableServices = servicesRepository.getAllServices()

                _state.update {
                    it.copy(
                        contract = contract,
                        contractServices = contractServices,
                        availableServices = availableServices,
                        isLoading = false,
                        error = null,
                        success = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        success = null
                    )
                }
            }
        }
    }


    fun handleIntent(intent: ContractsDetailIntent) {
        when (intent) {
            is ContractsDetailIntent.UpdateConditions ->
                updateConditions(intent.contractId, intent.conditions)

            is ContractsDetailIntent.AddService ->
                addServiceToContract(intent.contractId, intent.serviceId)

            is ContractsDetailIntent.RemoveService ->
                removeServiceFromContract(intent.contractId, intent.contractServiceId)

            ContractsDetailIntent.ClearMessages -> clearMessages()
        }
    }


    private fun updateConditions(contractId: Int, conditions: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, success = null) }

            try {
                contractsRepository.updateContractConditions(contractId, conditions)
                val updatedContract = contractsRepository.getContractById(contractId)

                _state.update {
                    it.copy(
                        contract = updatedContract,
                        isLoading = false,
                        error = null,
                        success = ContractsDetailSuccess.UpdateContract
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        success = null
                    )
                }
            }
        }
    }


    private fun addServiceToContract(contractId: Int, serviceId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, success = null) }

            try {
                servicesRepository.addServiceToContract(contractId, serviceId)
                val updatedContractServices = servicesRepository.getContractServices(contractId)

                _state.update {
                    it.copy(
                        contractServices = updatedContractServices,
                        isLoading = false,
                        error = null,
                        success = ContractsDetailSuccess.AddService
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        success = null
                    )
                }
            }
        }
    }


    private fun removeServiceFromContract(contractId: Int, contractServiceId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, success = null) }

            try {
                servicesRepository.removeServiceFromContract(contractServiceId)
                val updatedContractServices = servicesRepository.getContractServices(contractId)

                _state.update {
                    it.copy(
                        contractServices = updatedContractServices,
                        isLoading = false,
                        error = null,
                        success = ContractsDetailSuccess.RemoveService
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        success = null
                    )
                }
            }
        }
    }


    private fun clearMessages() {
        _state.update { it.copy(error = null, success = null) }
    }
}
