package com.example.urbane.ui.admin.payments.view.components

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.urbane.data.model.PaymentTransaction
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import com.example.urbane.utils.formatTransactionDate
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("DefaultLocale")
@Composable
fun TransactionItem(
    transaction: PaymentTransaction,
    viewModel: PaymentsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = when (transaction.method.lowercase()) {
                        "cash", "efectivo" -> Icons.Default.AttachMoney
                        "transfer", "transferencia" -> Icons.Default.AccountBalance
                        "card", "tarjeta" -> Icons.Default.CreditCard
                        else -> Icons.Default.Payment
                    },
                    contentDescription = transaction.method,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.method, fontWeight = FontWeight.Medium)

                Text(
                    "$${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                transaction.createdAt?.let {
                    Text(
                        formatTransactionDate(it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ✅ DESCARGAR
            IconButton(
                enabled = transaction.invoiceUrl != null,
                onClick = {
                    scope.launch {
                        val uri = viewModel.downloadInvoiceFromSupabase(
                            context,
                            transaction.invoiceUrl!!,
                            "factura_${transaction.id}.pdf"
                        )

                        if (uri != null) {
                            val result = snackbarHostState.showSnackbar(
                                message = "Factura ${transaction.id} descargada correctamente",
                                actionLabel = "Ir",
                                duration = SnackbarDuration.Long
                            )

                            if (result == SnackbarResult.ActionPerformed) {
                                context.startActivity(
                                    Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Download, contentDescription = "Descargar")
            }

            // ✅ COMPARTIR
            IconButton(
                enabled = transaction.invoiceUrl != null,
                onClick = {
                    scope.launch {
                        val uri = viewModel.downloadInvoiceFromSupabase(
                            context,
                            transaction.invoiceUrl!!,
                            "factura_${transaction.id}.pdf"
                        )

                        uri?.let {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, it)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                            context.startActivity(
                                Intent.createChooser(intent, "Compartir factura")
                            )
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
            }
        }
    }
}
