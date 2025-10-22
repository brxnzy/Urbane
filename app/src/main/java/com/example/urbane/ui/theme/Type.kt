package com.example.urbane.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.urbane.R

// Define la familia de fuentes Plus Jakarta Sans
val PlusJakartaSans = FontFamily(
    Font(R.font.plusjakartasans_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.plusjakartasans_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.plusjakartasans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.plusjakartasans_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.plusjakartasans_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.plusjakartasans_bolditalic, FontWeight.Bold, FontStyle.Italic)
)


val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)