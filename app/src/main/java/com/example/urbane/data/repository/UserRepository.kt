package com.example.urbane.data.repository


import android.util.Log
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residential
import com.example.urbane.data.model.Role
import com.example.urbane.data.model.User
import com.example.urbane.data.model.UserResidentialRole
import com.example.urbane.data.remote.supabase

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive


class UserRepository(val sessionManager: SessionManager) {

    suspend fun getUserRole(userId: String): Int? {
        return try {
            Log.d("UserRepository", "=== getUserRole llamado con userId: $userId ===")

            val response = supabase.from("users_residentials_roles")
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

    suspend fun getCurrentUser(currentUserId: String, currentUserEmail: String): UserResidentialRole? {
        try {

            Log.d("UserRepository","obteniendo datos del usuario, email $currentUserEmail id: $currentUserId")
            val user = supabase.from("users").select(columns = Columns.list("id", "name", "idCard" , "createdAt")){
                filter {
                    eq("id", currentUserId)
                }
            }.decodeSingle<User>()

            Log.d("UserRepository","datos del usuario $user")

            // 3. Obtener relación user-residential-role
            val urr = supabase.from("users_residentials_roles")
                .select(columns = Columns.list("user_id", "residential_id", "role_id")){

                filter {
                    eq("user_id", currentUserId)
                }
                }
                .decodeSingle<UrrIds>()



            Log.d("UserRepository","datos de la tabla relacional $urr")
            val residential = supabase.from("residentials")
                .select(columns = Columns.list("id", "name", "address", "phone", "logoUrl")){

                filter {
                    eq("id", urr.residential_id)
                }
                }
                .decodeSingle<Residential>()

            Log.d("UserRepository","datos del residencial $residential")

            val role = supabase.from("roles")
                .select(columns = Columns.list("id", "name")){

                filter {
                    eq("id", urr.role_id)
                }
                }
                .decodeSingle<Role>()

            Log.d("UserRepository","datos del rol $role")
            val userE = user.copy(email = currentUserEmail)

            return UserResidentialRole(
                user = userE,
                residential = residential,
                role = role
            )

        } catch (e: Exception) {
            Log.e("UserRepository", "error obteniendo la data del user ${e.message}")
            return null
        }
    }


}

@Serializable
data class UrrIds(
    val user_id: String,
    val residential_id: Int,
    val role_id: Int
)







