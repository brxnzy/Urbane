package com.example.urbane.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getResidenceIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "casa" -> Icons.Default.Home
        "apartamento".trim() -> Icons.Default.Apartment
        "local", "local comercial" -> Icons.Default.Storefront
        "villa" -> Icons.Default.HolidayVillage
        "terreno" -> Icons.Default.Terrain
        else -> Icons.Default.Home
    }
}