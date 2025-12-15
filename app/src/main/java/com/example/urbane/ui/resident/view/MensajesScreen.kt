package com.example.urbane.ui.resident.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajesScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Todos", "No leídos", "Importantes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Mensajes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "3 mensajes sin leer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(vertical = 12.dp),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(5) { index ->
                    MensajeCard(
                        remitente = when(index) {
                            0 -> "Administración"
                            1 -> "Conserjería"
                            2 -> "Administración"
                            3 -> "Mantenimiento"
                            else -> "Administración"
                        },
                        asunto = when(index) {
                            0 -> "Cambio en horario de recolección"
                            1 -> "Reunión de residentes"
                            2 -> "Actualización de seguridad"
                            3 -> "Mantenimiento programado"
                            else -> "Bienvenida al edificio"
                        },
                        preview = when(index) {
                            0 -> "Les informamos que el horario de recolección de basura ha sido modificado..."
                            1 -> "Se convoca a todos los residentes a la asamblea general del próximo mes..."
                            2 -> "Hemos implementado nuevas medidas de seguridad en el edificio..."
                            3 -> "El próximo viernes realizaremos mantenimiento en las áreas comunes..."
                            else -> "¡Bienvenido a nuestro edificio! Estamos aquí para ayudarte..."
                        },
                        fecha = when(index) {
                            0 -> "Hoy"
                            1 -> "Ayer"
                            2 -> "3d"
                            3 -> "1 sem"
                            else -> "2 sem"
                        },
                        hora = when(index) {
                            0 -> "10:30 AM"
                            1 -> "3:45 PM"
                            2 -> "9:15 AM"
                            3 -> "11:00 AM"
                            else -> "2:20 PM"
                        },
                        leido = index > 2,
                        importante = index == 1 || index == 2,
                        categoria = when(index) {
                            0, 2 -> "Anuncio"
                            1 -> "Reunión"
                            3 -> "Mantenimiento"
                            else -> "General"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MensajeCard(
    remitente: String,
    asunto: String,
    preview: String,
    fecha: String,
    hora: String,
    leido: Boolean,
    importante: Boolean,
    categoria: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (leido) 0.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (leido)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        border = if (!leido) {
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar con ícono según remitente
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (remitente) {
                            "Administración" -> Color(0xFF6200EE).copy(alpha = 0.15f)
                            "Conserjería" -> Color(0xFF03DAC6).copy(alpha = 0.15f)
                            "Mantenimiento" -> Color(0xFFFF6F00).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (remitente) {
                        "Administración" -> Icons.Default.AccountCircle
                        "Conserjería" -> Icons.Default.Info
                        "Mantenimiento" -> Icons.Default.Build
                        else -> Icons.Default.Message
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (remitente) {
                        "Administración" -> Color(0xFF6200EE)
                        "Conserjería" -> Color(0xFF03DAC6)
                        "Mantenimiento" -> Color(0xFFFF6F00)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header: Remitente y tiempo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            remitente,
                            fontWeight = if (leido) FontWeight.Medium else FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (importante) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Importante",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFA000)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            fecha,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = if (leido) FontWeight.Normal else FontWeight.SemiBold
                        )
                        Text(
                            hora,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Asunto
                Text(
                    asunto,
                    fontWeight = if (leido) FontWeight.Normal else FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Preview
                Text(
                    preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: Categoría
                Surface(
                    color = when (categoria) {
                        "Anuncio" -> Color(0xFF6200EE).copy(alpha = 0.12f)
                        "Reunión" -> Color(0xFF03DAC6).copy(alpha = 0.12f)
                        "Mantenimiento" -> Color(0xFFFF6F00).copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        categoria,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when (categoria) {
                            "Anuncio" -> Color(0xFF6200EE)
                            "Reunión" -> Color(0xFF03DAC6)
                            "Mantenimiento" -> Color(0xFFFF6F00)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Indicador de no leído
            if (!leido) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}