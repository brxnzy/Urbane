package com.example.urbane.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun formatTransactionDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""

    return try {
        val instant = Instant.parse(dateString)
        val localDT = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val months = listOf(
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        )

        val day = localDT.dayOfMonth.toString().padStart(2, '0')
        val month = months[localDT.monthNumber - 1]
        val year = localDT.year

        val hour = localDT.hour.toString().padStart(2, '0')
        val minute = localDT.minute.toString().padStart(2, '0')
        val second = localDT.second.toString().padStart(2, '0')

        "$day $month $year, $hour:$minute:$second"

    } catch (e: Exception) {
        dateString
    }
}