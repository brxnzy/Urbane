package com.example.urbane.ui.admin.settings.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.settings.model.SurveysIntent
import com.example.urbane.ui.admin.settings.viewmodel.SurveysViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyBottomSheet(
    viewModel: SurveysViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Crear Encuesta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Pregunta
            OutlinedTextField(
                value = state.question,
                onValueChange = {
                    viewModel.processIntent(SurveysIntent.UpdateQuestion(it))
                },
                label = { Text("Pregunta") },
                placeholder = { Text("¿Cuál es tu opinión sobre...?") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.question.isBlank() && state.question.isNotEmpty(),
                supportingText = {
                    if (state.question.isBlank() && state.question.isNotEmpty()) {
                        Text("La pregunta no puede estar vacía")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Opciones
            Text(
                text = "Opciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = option,
                        onValueChange = {
                            viewModel.processIntent(
                                SurveysIntent.UpdateOption(index, it)
                            )
                        },
                        label = { Text("Opción ${index + 1}") },
                        placeholder = { Text("Ingresa una opción") },
                        modifier = Modifier.weight(1f),
                        isError = option.isBlank() && option.isNotEmpty(),
                        supportingText = {
                            if (option.isBlank() && option.isNotEmpty()) {
                                Text("No puede estar vacía")
                            }
                        }
                    )

                    // Solo mostrar botón de eliminar si hay más de 2 opciones
                    if (state.options.size > 2) {
                        IconButton(
                            onClick = {
                                viewModel.processIntent(SurveysIntent.RemoveOption(index))
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar opción",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // Espaciador para mantener el diseño uniforme
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para agregar opción
            OutlinedButton(
                onClick = {
                    viewModel.processIntent(SurveysIntent.AddOption)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Agregar opción")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Validaciones y mensajes de ayuda
            if (!state.canSave && state.question.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Para crear la encuesta:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.question.isBlank()) {
                        Text(
                            text = "• Escribe una pregunta",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    if (state.options.any { it.isBlank() }) {
                        Text(
                            text = "• Completa todas las opciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    if (state.options.size < 2) {
                        Text(
                            text = "• Agrega al menos 2 opciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    if (state.options.distinct().size != state.options.size) {
                        Text(
                            text = "• Las opciones no pueden estar duplicadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón de Crear
            Button(
                onClick = {
                    viewModel.processIntent(SurveysIntent.CreateSurvey)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Creando...")
                } else {
                    Text("Crear Encuesta")
                }
            }

            // Mensaje de error
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}