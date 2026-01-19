package com.example.urbane.ui.admin.finances.view
import ExpenseCard
import FinancesViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.urbane.R
import com.example.urbane.ui.admin.finances.model.FinancesIntent
import com.example.urbane.ui.admin.finances.view.components.RegisterExpenseBottomSheet
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: FinancesViewModel) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
        viewModel.loadBalance()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.loadBalance()
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    tint = Color.White,
                    contentDescription = stringResource(R.string.registrar_egreso)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error al cargar egresos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                state.expenses.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay egresos registrados",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presiona + para registrar un egreso",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEE2E2)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Total de Egresos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF7F1D1D),
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "$${"%.2f".format(state.expenses.sumOf { it.amount })}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFF991B1B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        items(
                            items = state.expenses,
                            key = { it.id ?: it.hashCode() }
                        ) { expense ->
                            ExpenseCard(expense)
                        }
                    }
                }

            }
        }
    }

    if (showBottomSheet) {
        RegisterExpenseBottomSheet(
            viewModel = viewModel,
            sheetState = sheetState,
            onDismiss = {
                showBottomSheet = false
                viewModel.handleIntent(FinancesIntent.UpdateAmount(""))
                viewModel.handleIntent(FinancesIntent.UpdateDescription(""))
            }
        )
    }
}

