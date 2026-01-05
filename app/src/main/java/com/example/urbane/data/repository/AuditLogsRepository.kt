package com.example.urbane.data.repository

import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.AuditLog
import com.example.urbane.data.remote.supabase
import com.example.urbane.utils.getResidentialId
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.JsonObject

class AuditLogsRepository(val sessionManager: SessionManager) {

    suspend fun getAllLogs(): List<AuditLog> {
        try {
            val residentialId = getResidentialId(sessionManager)
            val logs = supabase.from("logs").select(columns = Columns.list(
                "id",
                "adminId",
                "action",
                "entity",
                "entityId",
                "data",
                "createdAt",
                "residentialId",
                "admin:users(id, name, photoUrl)",
            )) {
                filter {
                    eq("residentialId", residentialId!!)
                }
                    order("createdAt", Order.DESCENDING)
            }.decodeList<AuditLog>()
            return logs
        } catch (e: Exception) {
            Log.e("AuditLogsRepository", "Error en getAllLogs: $e")
            throw e

        }
    }

    suspend fun logAction(
        action: String,
        entity: String,
        entityId: String? = null,
        data: JsonObject? = null
    ) {
        try {
            val user = sessionManager.sessionFlow.firstOrNull()
                ?: return

            val residentialId = getResidentialId(sessionManager)
                ?: return

            val log = AuditLog(
                adminId = user.userData?.user?.id!!,
                action = action,
                entity = entity,
                entityId = entityId,
                data = data,
                residentialId = residentialId
            )

            supabase
                .from("logs")
                .insert(log)

        } catch (e: Exception) {
            Log.e("AuditLogsRepository", "Error en logAction: $e")

        }
    }
}
