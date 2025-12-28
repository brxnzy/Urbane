package com.example.urbane.ui.admin.incidents.view.components

import androidx.compose.ui.graphics.Color

fun getStatusColor(status: String?): Color {
    return when (status?.lowercase()) {
        "pendiente", "pending" -> Color(0xFFFBBF24) // Amarillo
        "revisado", "reviewed" -> Color(0xFFF97316) // Naranja
        "en curso", "in_progress" -> Color(0xFF3B82F6) // Azul
        "resuelto", "resolved" -> Color(0xFF10B981) // Verde
        "rechazado", "rejected" -> Color(0xFFEF4444) // Rojo
        else -> Color(0xFF9CA3AF) // Gris claro
    }
}
