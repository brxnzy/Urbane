package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.SupabaseClient

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive


class UserRepository {

    suspend fun getUserRole(userId: String): Int? {
        return try {
            Log.d("UserRepository", "=== getUserRole llamado con userId: $userId ===")

            val response = supabase.from("users_roles")
                .select(columns = Columns.list("role_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<JsonObject>()

            Log.d("UserRepository", "Respuesta cruda de Supabase: $response")

            response["role_id"]?.jsonPrimitive?.int
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al obtener role_id", e)
            null
        }
    }
}








