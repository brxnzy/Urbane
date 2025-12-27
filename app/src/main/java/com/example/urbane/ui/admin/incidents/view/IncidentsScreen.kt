package com.example.urbane.ui.admin.incidents.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeTestMessage(
    val id: String,
    val message: String,
    val created_at: String? = null
)

@Composable
fun IncidentsScreen() {
    val incidents = remember { mutableStateListOf<String>() }
    var status by remember { mutableStateOf("Conectando...") }

    LaunchedEffect(Unit) {
        try {
            Log.d("Realtime", "Creando canal...")

            val channel = supabase.channel("test-channel")

            Log.d("Realtime", "Configurando listener...")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "realtime_test"
            }

            Log.d("Realtime", "Suscribiéndose al canal...")
            channel.subscribe(blockUntilSubscribed = true)

            Log.d("Realtime", "Canal suscrito exitosamente")
            status = "✅ Conectado"

            changeFlow.onEach { action ->
                Log.d("Realtime", "Cambio detectado: ${action::class.simpleName}")

                when (action) {
                    is PostgresAction.Insert -> {
                        try {
                            // 🔥 Decodificar el record correctamente
                            val decoded = action.decodeRecord<RealtimeTestMessage>()
                            Log.d("Realtime", "INSERT - ID: ${decoded.id}, Message: ${decoded.message}")
                            incidents.add(0, decoded.message)
                        } catch (e: Exception) {
                            Log.e("Realtime", "Error decodificando: ${e.message}")
                        }
                    }
                    is PostgresAction.Update -> {
                        val decoded = action.decodeRecord<RealtimeTestMessage>()
                        Log.d("Realtime", "UPDATE - Message: ${decoded.message}")
                    }
                    is PostgresAction.Delete -> {
                        val decoded = action.decodeOldRecord<RealtimeTestMessage>()
                        Log.d("Realtime", "DELETE - ID: ${decoded.id}")
                    }

                    else -> {}
                }
            }.launchIn(this)

        } catch (e: Exception) {
            Log.e("Realtime", "Error: ${e.message}", e)
            status = "❌ Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Estado: $status",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Divider()

        Text(
            text = "Mensajes recibidos: ${incidents.size}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (incidents.isEmpty()) {
            Text(
                text = "Esperando mensajes...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(incidents) { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}