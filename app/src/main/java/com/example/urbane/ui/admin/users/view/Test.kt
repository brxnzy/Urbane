package com.example.urbane.ui.admin.users.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TestScreen() {
    var resultado by remember { mutableStateOf<String>("Presiona el botón") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                loading = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = supabase.functions.invoke("test")
                        resultado = response.body()
                    } catch (e: Exception) {
                        resultado = "Error: ${e.message}"
                    }
                    loading = false
                }
            },
            enabled = !loading
        ) {
            Text("Llamar Edge Function")
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            Text(resultado)
        }
    }
}

