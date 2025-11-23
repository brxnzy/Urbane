package com.example.urbane.utils

import androidx.annotation.StringRes
import com.example.urbane.R


@StringRes
fun getRoleLabelRes(role: String?): Int {
    return when (role) {
        "admin" -> R.string.role_admin
        "resident" -> R.string.role_resident
        "owner" -> R.string.role_owner
        else -> "Sin rol"
    } as Int
}
