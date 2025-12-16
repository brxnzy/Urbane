package com.example.urbane.ui.admin.fines.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.data.model.Fine
import com.example.urbane.ui.admin.fines.viewmodel.FinesViewModel

@Composable
fun FinesScreen(
    viewModel: FinesViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFines()
    }

    when {
        state.isLoading -> {
            CircularProgressIndicator()
        }

        state.error != null -> {
            Text(text = state.error!!)
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(state.fines) { fine ->
                    FineCard(fine)
                }
            }
        }
    }
}


@Composable
fun FineCard(fine: Fine) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = fine.title,
                fontWeight = FontWeight.Bold
            )

            fine.description?.let {
                Text(text = it)
            }

            Text(text = "Monto: ${fine.amount}")

            fine.resident?.name.let {
                Text(text = "Residente: $it")
            }

            if (fine.paymentId == null) {
                Text(
                    text = "⚠ Se aplicará en el próximo pago",
                    fontSize = 12.sp
                )
            }
        }
    }
}
