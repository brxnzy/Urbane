package com.example.urbane.ui.admin.contracts.model

import androidx.datastore.preferences.protobuf.LazyStringArrayList.emptyList
import com.example.urbane.data.model.Contract

data class ContractsDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val contract: Contract? = null

)
