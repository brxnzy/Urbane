package com.example.urbane.ui.admin.contracts.view.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.urbane.R
import com.example.urbane.data.model.Contract
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ContractCard(
    modifier: Modifier = Modifier,
    contract: Contract,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    if (contract.residentPhotoUrl != null) {
                        AsyncImage(
                            model = contract.residentPhotoUrl,
                            contentDescription = "Foto de ${contract.residentName}",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.usuario_sin_foto),
                            modifier = Modifier.size(78.dp),
                            tint = MaterialTheme.colorScheme.onTertiary
                        )

                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = contract.residentName ?: "Sin nombre",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = contract.residenceName ?: "Sin residencia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Chevron Icon
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Date + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${formatDate(contract.startDate)} - ${contract.endDate?.let { formatDate(it) } ?: "Indefinido"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                // Status Badge
                StatusBadge(status = getContractStatus(contract))
            }
        }
    }
}

@Composable
fun StatusBadge(status: ContractStatus) {
    val (backgroundColor, textColor) = when (status) {
        ContractStatus.ACTIVE -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        ContractStatus.FINALIZED -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)

    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = when (status) {
                ContractStatus.ACTIVE -> "Activo"
                ContractStatus.FINALIZED -> "Finalizado"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp
        )
    }
}

// Helper Functions
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun getContractStatus(contract: Contract): ContractStatus {
    val today = Calendar.getInstance()

    val startDate = try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        Calendar.getInstance().apply {
            time = format.parse(contract.startDate) ?: return ContractStatus.FINALIZED
        }
    } catch (e: Exception) {
        return ContractStatus.FINALIZED
    }

    val endDate = contract.endDate?.let {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Calendar.getInstance().apply {
                time = format.parse(it) ?: return@let null
            }
        } catch (e: Exception) {
            null
        }
    }

    return when {
        endDate == null -> ContractStatus.ACTIVE
        today.after(endDate) -> ContractStatus.FINALIZED
        else -> ContractStatus.ACTIVE
    }
}

enum class ContractStatus {
    ACTIVE,
    FINALIZED
}