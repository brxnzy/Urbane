package com.example.urbane.ui.admin.incidents.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urbane.data.model.Incident
import com.example.urbane.ui.common.getStatusColor
import com.example.urbane.utils.formatDate

@Composable
fun IncidentCard(
    incident: Incident,
    onAttendClick: (Incident) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getStatusColor(incident.status)
                ) {
                    Text(
                        text = incident.status ?: "Pendiente",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!incident.category.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Text(
                        text = incident.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            if (incident.imageUrls?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(incident.imageUrls) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedImageUrl = imageUrl },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Usar BoxWithConstraints para adaptar el layout según el ancho disponible
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val maxWidth = maxWidth
                val shouldStack = maxWidth < 360.dp // Si el ancho es menor a 360dp, apilar verticalmente

                if (shouldStack) {
                    // Layout vertical para pantallas pequeñas
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = ("Por: " + incident.residentName["name"]),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (incident.createdAt != null) {
                                Text(
                                    text = formatDate(incident.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(top = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))


                        }

                        if (incident.status == "Pendiente") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onAttendClick(incident) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Atender")
                                }
                                Button(
                                    onClick = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = ("Por: " + incident.residentName["name"]),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (incident.createdAt != null) {
                                Text(
                                    text = formatDate(incident.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(top = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if(incident.status == "Atendido") {
                            Spacer(modifier = Modifier.height(3.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7) // Verde claro
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📅",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            text = "Pautada: ${incident.scheduledDate} ${incident.startTime}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF166534),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            }


                        if (incident.status == "Pendiente") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onAttendClick(incident) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Atender")
                                }
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedImageUrl?.let { imageUrl ->
            ImageViewerDialog(
                imageUrl = imageUrl,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }
}