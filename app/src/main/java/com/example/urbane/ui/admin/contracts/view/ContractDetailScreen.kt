package com.example.urbane.ui.admin.contracts.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsDetailViewModel

@Composable
fun ContractDetailScreen(
    contractId: String,
    viewmodel: ContractsDetailViewModel
){

    val state by viewmodel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewmodel.loadContract(contractId.toInt())
    }

    Text("$state.contract?.id")

}