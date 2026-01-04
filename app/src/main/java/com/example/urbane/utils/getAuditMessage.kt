package com.example.urbane.utils

import android.content.Context
import com.example.urbane.R
import com.example.urbane.data.model.AuditLog
import kotlinx.serialization.json.jsonPrimitive

fun getAuditMessage(
    context: Context,
    log: AuditLog
): String {
    val data = log.data

    return when (log.action) {

        "RESIDENCE_CREATED" -> {
            val name = data?.get("name")?.jsonPrimitive?.content ?: ""
            context.getString(
                R.string.audit_residence_created,
                name
            )
        }


        else -> {
            context.getString(R.string.audit_unknown)
        }
    }
}
