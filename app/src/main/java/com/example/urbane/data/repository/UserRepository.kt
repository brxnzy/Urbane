package com.example.urbane.data.repository


import com.example.urbane.data.remote.supabase

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class userRepository {
    suspend fun getUserRole(userId: String): String {
        return try {
            val response = supabase.from("users_roles")
                .select(columns = Columns.raw("roles(name)")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<JsonObject>()

            response["roles"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                ?: throw Exception("Nombre del rol no encontrado")

        } catch (e: Exception) {
            throw Exception("Error al obtener el rol del usuario: ${e.message}")
        }
    }


    }

