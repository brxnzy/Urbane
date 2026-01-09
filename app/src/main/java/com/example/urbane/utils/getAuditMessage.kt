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

        "USER_DISABLED" -> {
            val userName = data?.get("userName")?.jsonPrimitive?.content ?: ""
            val userEmail = data?.get("userEmail")?.jsonPrimitive?.content ?: ""
            context.getString(
                R.string.audit_user_disabled,
                userName,
                userEmail
            )
        }

        "USER_ENABLED" -> {
            val userName = data?.get("userName")?.jsonPrimitive?.content ?: ""
            val userEmail = data?.get("userEmail")?.jsonPrimitive?.content ?: ""
            val residenceName = data?.get("residenceName")?.jsonPrimitive?.content

            if (residenceName != null) {
                context.getString(
                    R.string.audit_user_enabled_with_residence,
                    userName,
                    userEmail,
                    residenceName
                )
            } else {
                context.getString(
                    R.string.audit_user_enabled,
                    userName,
                    userEmail
                )
            }
        }

        "USER_ROLE_UPDATED" -> {
            val userName = data?.get("userName")?.jsonPrimitive?.content ?: ""
            val userEmail = data?.get("userEmail")?.jsonPrimitive?.content ?: ""
            val oldRole = data?.get("oldRole")?.jsonPrimitive?.content ?: ""
            val newRole = data?.get("newRole")?.jsonPrimitive?.content ?: ""
            val residenceName = data?.get("residenceName")?.jsonPrimitive?.content

            val oldRoleTranslated = translateRole(context, oldRole)
            val newRoleTranslated = translateRole(context, newRole)

            if (residenceName != null) {
                context.getString(
                    R.string.audit_user_role_updated_with_residence,
                    userName,
                    userEmail,
                    oldRoleTranslated,
                    newRoleTranslated,
                    residenceName
                )
            } else {
                context.getString(
                    R.string.audit_user_role_updated,
                    userName,
                    userEmail,
                    oldRoleTranslated,
                    newRoleTranslated
                )
            }
        }

        "CONTRACT_UPDATED" -> {
            val residentName = data?.get("residentName")?.jsonPrimitive?.content ?: ""
            val residenceName = data?.get("residenceName")?.jsonPrimitive?.content ?: ""
            val oldConditions = data?.get("oldConditions")?.jsonPrimitive?.content ?: ""
            val newConditions = data?.get("newConditions")?.jsonPrimitive?.content ?: ""
            val oldAmount = data?.get("oldAmount")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            val newAmount = data?.get("newAmount")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

            context.getString(
                R.string.audit_contract_updated,
                residentName,
                residenceName,
                oldConditions,
                newConditions,
                oldAmount,
                newAmount
            )
        }

        "PAYMENT_REGISTERED" -> {
            val residentName = data?.get("residentName")?.jsonPrimitive?.content ?: ""
            val month = data?.get("month")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val year = data?.get("year")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val amountPaid = data?.get("amountPaid")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            val previousStatus = data?.get("previousStatus")?.jsonPrimitive?.content ?: ""
            val newStatus = data?.get("newStatus")?.jsonPrimitive?.content ?: ""

            context.getString(
                R.string.audit_payment_registered,
                amountPaid,
                residentName,
                month,
                year,
                previousStatus,
                newStatus
            )
        }

        else -> {
            context.getString(R.string.audit_unknown)
        }
    }
}

fun translateRole(context: Context, role: String): String {
    return when (role) {
        "admin" -> context.getString(R.string.role_admin)
        "resident" -> context.getString(R.string.role_resident)
        else -> context.getString(R.string.role_unknown)
    }
}
