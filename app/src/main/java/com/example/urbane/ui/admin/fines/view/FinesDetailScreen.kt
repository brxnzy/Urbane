package com.example.urbane.ui.admin.fines.view
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.fines.model.FineDetailIntent
import com.example.urbane.ui.admin.fines.viewmodel.FinesDetailViewModel
import com.example.urbane.ui.common.InfoSection2
import com.example.urbane.ui.common.UserInfoItem2
import com.example.urbane.utils.formatDate
import com.example.urbane.utils.intToMonth
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FinesDetailScreen(
    fineId: String,
    viewModel: FinesDetailViewModel,
    goBack: () -> Unit
) {
    LaunchedEffect(fineId) {
        viewModel.loadFine(fineId.toInt())
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            snackbarHostState.showSnackbar("Multa cancelada")
            viewModel.resetSuccess()
        }
    }


    val yellow = Color(0xFFFFC107)
    val red = Color(0xFFF44336)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!state.isLoading && state.fine != null) {
                            "Multa de ${state.fine!!.resident?.name}"
                        } else {
                            "Multa"
                        },
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goBack() }) { // Sin mensaje al volver normal
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }

            state.fine != null -> {
                val fine = state.fine!!
                val status = fine.status.lowercase()

                val bgColor = when (status) {
                    "pagado", "paid" ->
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

                    "pendiente", "pending" ->
                        yellow.copy(alpha = 0.20f)

                    "cancelada", "canceled", "cancelled" ->
                        red.copy(alpha = 0.20f)

                    else ->
                        MaterialTheme.colorScheme.surfaceVariant
                }

                val icon = when (status) {
                    "pagado", "paid" -> Icons.Default.CheckCircle
                    "pendiente", "pending" -> Icons.Default.Schedule
                    "cancelada", "canceled", "cancelled" -> Icons.Default.Cancel
                    else -> Icons.Default.Receipt
                }

                val iconTint = when (status) {
                    "pagado", "paid" ->
                        MaterialTheme.colorScheme.primary

                    "pendiente", "pending" ->
                        yellow

                    "cancelada", "canceled", "cancelled" ->
                        red

                    else ->
                        MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(bgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = fine.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,

                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = fine.status.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = iconTint
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    InfoSection2 {

                        UserInfoItem2(
                            label = "Monto",
                            value = "RD$ ${fine.amount}"
                        )

                        UserInfoItem2(
                            label = "Descripción",
                            value = fine.description ?: "No disponible"
                        )

                        UserInfoItem2(
                            label = "Residente",
                            value = fine.resident?.name ?: "No asignado"
                        )

                        UserInfoItem2(
                            label = "Fecha",
                            value = formatDate(fine.createdAt)
                        )

                        UserInfoItem2(
                            label = "Pago",
                            value = fine.paymentPeriod?.let {
                                "${intToMonth(it.month)} ${it.year}"
                            } ?: "No asociado"
                        )

                    }
                    if (status == "pendiente" || status == "pending") {

                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.handleIntent(FineDetailIntent.CancelFine)},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                }
            }
        }
    }
}
