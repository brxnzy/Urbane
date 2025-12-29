package com.example.urbane.ui.common

import androidx.compose.ui.graphics.Color

fun getStatusColor(status: String?): Color {
    return when (status?.lowercase()) {
        "pendiente" -> Color(0xFFFBBF24) // Amarillo
        "atendido" -> Color(0xFFF97316) // Naranja
        "en curso" -> Color(0xFF3B82F6) // Azul
        "resuelto" -> Color(0xFF10B981) // Verde
        "rechazado" -> Color(0xFFEF4444) // Rojo
        else -> Color(0xFF9CA3AF) // Gris claro
    }
}
