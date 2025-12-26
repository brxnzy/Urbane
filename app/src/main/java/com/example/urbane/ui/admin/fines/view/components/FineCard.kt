package com.example.urbane.ui.admin.fines.view.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbane.data.model.Fine

@Composable
fun FineCard(
    fine: Fine,
    onClick: () -> Unit
) {
    val status = fine.status.lowercase()

    val yellow = Color(0xFFFFC107)
    val red = Color.Red

    val bgColor = when (status) {
        "pagado", "paid" ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

        "pendiente", "pending" ->
            yellow.copy(alpha = 0.20f)

        "cancelada", "canceled", "cancelled" ->
            red.copy(alpha = 0.20f)

        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when (status) {
        "pagado", "paid" -> Icons.Default.CheckCircle
        "pendiente", "pending" -> Icons.Default.Schedule
        "cancelada", "canceled", "cancelled" -> Icons.Default.Cancel
        else -> Icons.Default.Receipt
    }

    val iconTint = when (status) {
        "pagado", "paid" ->
            MaterialTheme.colorScheme.primary

        "pendiente", "pending" ->
            yellow

        "cancelada", "canceled", "cancelled" ->
            red

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ICONO
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            // INFO
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fine.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                fine.resident?.let { resident ->
                    Text(
                        text = resident.name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = bgColor
                    ) {
                        Text(
                            text = fine.status.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = iconTint,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }

                    if (fine.paymentId == null && status == "pendiente") {
                        Text(
                            text = "⚠ Próximo pago",
                            fontSize = 11.sp,
                            color = yellow
                        )
                    }
                }
            }

            // MONTO
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.2f", fine.amount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // FLECHA
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}