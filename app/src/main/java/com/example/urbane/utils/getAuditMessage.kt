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

        "RESIDENCE_UPDATED" -> {
            val oldName = data?.get("oldName")?.jsonPrimitive?.content ?: ""
            val newName = data?.get("newName")?.jsonPrimitive?.content ?: ""
            val oldType = data?.get("oldType")?.jsonPrimitive?.content ?: ""
            val newType = data?.get("newType")?.jsonPrimitive?.content ?: ""
            val oldDescription = data?.get("oldDescription")?.jsonPrimitive?.content ?: ""
            val newDescription = data?.get("newDescription")?.jsonPrimitive?.content ?: ""

            context.getString(
                R.string.audit_residence_updated,
                oldName,
                newName,
                oldType,
                newType,
                oldDescription,
                newDescription
            )
        }

        "RESIDENCE_VACATED" -> {
            val residenceName = data?.get("residenceName")?.jsonPrimitive?.content ?: ""
            context.getString(
                R.string.audit_residence_vacated,
                residenceName
            )
        }

        "RESIDENCE_DELETED" -> {
            val name = data?.get("name")?.jsonPrimitive?.content ?: ""
            context.getString(
                R.string.audit_residence_deleted,
                name
            )
        }

        else -> {
            context.getString(R.string.audit_unknown)
        }
    }
}
