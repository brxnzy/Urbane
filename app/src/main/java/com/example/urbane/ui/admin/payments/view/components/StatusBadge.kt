package com.example.urbane.ui.admin.payments.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(status: String) {
    val estadoColor = when (status.lowercase()) {
        "pagado" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "parcial" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "pendiente" -> Color(0xFFFFF9C4) to Color(0xFFE65100)
        else -> Color.LightGray to Color.DarkGray
    }

    Surface(
        color = estadoColor.first,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = estadoColor.second,
            fontWeight = FontWeight.Bold
        )
    }
}