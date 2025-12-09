package com.example.urbane.ui.admin.payments.view

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.urbane.data.model.InvoiceData
import com.example.urbane.ui.admin.payments.model.PaymentSuccess
import com.example.urbane.ui.admin.payments.model.PaymentsIntent
import com.example.urbane.ui.admin.payments.view.components.PendingPaymentCard
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.get
import androidx.core.net.toUri


@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(viewModel: PaymentsViewModel) {
    var residentExpanded by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadResidents()
    }

    val context = LocalContext.current


    var showDialog by remember { mutableStateOf(false) }
    var invoiceFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(state.success) {
        val success = state.success
        if (success is PaymentSuccess.InvoiceGenerated) {
            // Generamos y subimos el PDF a Supabase
            val file = File(context.cacheDir, "factura_${success.invoice.invoiceId}.pdf")
            viewModel.generateInvoicePdf(context, success.invoice) // ya lo genera local y sube

            invoiceFile = file
            showDialog = true
            viewModel.clearSuccess()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showDialog && invoiceFile != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pago realizado correctamente") },
            text = { Text("Se ha generado la factura exitosamente.") },
            confirmButton = {
                TextButton(onClick = {

                    val invoice = invoiceFile!!

                    // Guardar PDF en Descargas usando MediaStore
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, invoice.name) // nombre del archivo
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.IS_PENDING, 1) // archivo en proceso de escritura
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { out ->
                            invoice.inputStream().copyTo(out)
                        }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0) // archivo completo
                        context.contentResolver.update(uri, values, null, null)
                    }

                    // Cerrar el dialog
                    showDialog = false

                    // Mostrar Snackbar con acción para abrir el PDF
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Factura descargada",
                            actionLabel = "Abrir"
                        )
                        if (result == SnackbarResult.ActionPerformed && uri != null) {
                            // Abrir el PDF directamente usando FileProvider
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }

                }) {
                    Text("Descargar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val uri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        invoiceFile!!
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir factura"))
                    // Cerrar el dialog al compartir
                    showDialog = false
                }) {
                    Text("Compartir")
                }
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                // Selección de residente
                Text(
                    "RESIDENTE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = residentExpanded,
                    onExpandedChange = { residentExpanded = !residentExpanded }
                ) {
                    OutlinedTextField(
                        value = state.selectedResident?.name ?: "Seleccione un residente",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(residentExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = residentExpanded,
                        onDismissRequest = { residentExpanded = false }
                    ) {
                        state.residents.forEach { resident ->
                            DropdownMenuItem(
                                text = { Text(resident.name) },
                                onClick = {
                                    viewModel.handleIntent(
                                        PaymentsIntent.SelectResident(resident)
                                    )
                                    residentExpanded = false
                                }
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Mostrar pagos pendientes
                if (state.selectedResident != null && !state.isLoading) {
                    Spacer(Modifier.height(32.dp))

                    Text(
                        "MESES PENDIENTES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))

                    state.pendingPayments.forEach { payment ->
                        PendingPaymentCard(
                            payment = payment,
                            isSelected = state.selectedPayments.containsKey(payment.id),
                            selectedPayment = state.selectedPayments[payment.id],
                            onToggleSelection = {
                                viewModel.handleIntent(PaymentsIntent.TogglePaymentSelection(payment))
                            },
                            onAmountChange = { newAmount ->
                                payment.id?.let { id ->
                                    viewModel.handleIntent(
                                        PaymentsIntent.UpdatePaymentAmount(id, newAmount)
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (state.pendingPayments.isEmpty()) {
                        Text(
                            "Este residente no tiene pagos pendientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }

                    // Botón de registrar pagos (solo si hay pagos seleccionados)
                    if (state.selectedPayments.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))

                        val totalAPagar =
                            state.selectedPayments.values.sumOf { it.montoPagar.toDouble() }
                                .toFloat()

                        Text(
                            "TOTAL A PAGAR: RD$ %.2f".format(totalAPagar),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.handleIntent(PaymentsIntent.RegisterPayments)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Registrar Pago en Efectivo")
                        }
                    }
                }

                // Mostrar error
                state.errorMessage?.let { error ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}