package com.example.urbane.ui.admin.incidents.view.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.ui.admin.incidents.model.IncidentsIntent
import com.example.urbane.ui.admin.incidents.viewmodel.IncidentsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendIncidentBottomSheet(
    viewModel: IncidentsViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val incident = state.selectedIncident ?: return
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }

    val isFormValid by remember(state.scheduledDate, state.startTime, state.adminResponse) {
        derivedStateOf {
            state.scheduledDate.isNotEmpty() &&
                    state.startTime.isNotEmpty() &&
                    state.adminResponse.isNotEmpty()
        }
    }

    // Expandir el sheet completamente cuando se abre
    LaunchedEffect(Unit) {
        scope.launch {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.handleIntent(IncidentsIntent.ClearSelection)
            onDismiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
        ) {
            // Header
            Text(
                text = "Atender Incidencia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = incident.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fecha programada
            OutlinedTextField(
                value = state.scheduledDate,
                onValueChange = {},
                label = { Text("Fecha programada") },
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()

                            if (state.scheduledDate.isNotEmpty()) {
                                try {
                                    val date = LocalDate.parse(state.scheduledDate)
                                    calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
                                } catch (e: Exception) {
                                    // Si hay error, usar fecha actual
                                }
                            }

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    viewModel.handleIntent(
                                        IncidentsIntent.UpdateScheduledDate(selectedDate.toString())
                                    )

                                    // Si es hoy, limpiar la hora para forzar una nueva selección
                                    if (selectedDate.isEqual(today) && state.startTime.isNotEmpty()) {
                                        val selectedTime = LocalTime.parse(state.startTime)
                                        if (selectedTime.isBefore(currentTime)) {
                                            viewModel.handleIntent(IncidentsIntent.UpdateStartTime(""))
                                        }
                                    }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis()
                                show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Seleccione una fecha") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hora de inicio
            OutlinedTextField(
                value = state.startTime,
                onValueChange = {},
                label = { Text("Hora de inicio") },
                readOnly = true,
                enabled = state.scheduledDate.isNotEmpty(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()

                            if (state.startTime.isNotEmpty()) {
                                try {
                                    val time = LocalTime.parse(state.startTime)
                                    calendar.set(Calendar.HOUR_OF_DAY, time.hour)
                                    calendar.set(Calendar.MINUTE, time.minute)
                                } catch (e: Exception) {
                                    // Si hay error, usar hora actual
                                }
                            }

                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val selectedTime = LocalTime.of(hourOfDay, minute)
                                    val selectedDate = LocalDate.parse(state.scheduledDate)

                                    // Validar que si es hoy, la hora no haya pasado
                                    if (selectedDate.isEqual(today) && selectedTime.isBefore(currentTime)) {
                                        return@TimePickerDialog
                                    }

                                    viewModel.handleIntent(
                                        IncidentsIntent.UpdateStartTime(
                                            selectedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                                        )
                                    )
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        enabled = state.scheduledDate.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Seleccionar hora"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Seleccione una hora") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Respuesta del admin
            OutlinedTextField(
                value = state.adminResponse,
                onValueChange = {
                    viewModel.handleIntent(IncidentsIntent.UpdateAdminResponse(it))
                },
                label = { Text("Respuesta/Notas") },
                placeholder = { Text("Describa las acciones a tomar...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                minLines = 3
            )

            // Error message
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.handleIntent(IncidentsIntent.ClearSelection)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isProcessing
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        viewModel.handleIntent(IncidentsIntent.AttendIncident)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && !state.isProcessing
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Atender")
                    }
                }
            }
        }
    }
}