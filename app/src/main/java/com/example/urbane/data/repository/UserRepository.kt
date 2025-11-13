package com.example.urbane.data.repository


import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.Residential
import com.example.urbane.data.model.Role
import com.example.urbane.data.model.UrrIds
import com.example.urbane.data.model.User
import com.example.urbane.data.model.UserResidentialRole
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val idCard: String,
    val password: String,
    val role_id: Int,
    val residence_id: Int? = null,
    val residential_id: Int
)


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

    suspend fun getCurrentUser(
        currentUserId: String,
        currentUserEmail: String
    ): UserResidentialRole? {
        try {

            Log.d(
                "UserRepository",
                "obteniendo datos del usuario, email $currentUserEmail id: $currentUserId"
            )
            val user = supabase.from("users")
                .select(columns = Columns.list("id", "name", "idCard", "createdAt")) {
                    filter {
                        eq("id", currentUserId)
                    }
                }.decodeSingle<User>()

            Log.d("UserRepository", "datos del usuario $user")

            // 3. Obtener relación user-residential-role
            val urr = supabase.from("users_residentials_roles")
                .select(columns = Columns.list("user_id", "residential_id", "role_id")) {

                    filter {
                        eq("user_id", currentUserId)
                    }
                }
                .decodeSingle<UrrIds>()



            Log.d("UserRepository", "datos de la tabla relacional $urr")
            val residential = supabase.from("residentials")
                .select(columns = Columns.list("id", "name", "address", "phone", "logoUrl")) {

                    filter {
                        eq("id", urr.residential_id)
                    }
                }
                .decodeSingle<Residential>()

            Log.d("UserRepository", "datos del residencial $residential")

            val role = supabase.from("roles")
                .select(columns = Columns.list("id", "name")) {

                    filter {
                        eq("id", urr.role_id)
                    }
                }
                .decodeSingle<Role>()

            Log.d("UserRepository", "datos del rol $role")
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


    suspend fun createUser(
        name: String,
        email: String,
        idCard: String,
        password: String,
        roleId: Int = 1,
        residenceId: Int?
    ): Any? { // null = éxito, texto = error

        val client = HttpClient(Android) {
            install(ContentNegotiation) { json() }
        }

        return try {
            val user = sessionManager.sessionFlow.firstOrNull()
                ?: return "No hay sesión activa"

            val residentialId = user.userData?.residential?.id
                ?: return "No se encontró el ID del residencial"

            val body =
                CreateUserRequest(name, email, idCard, password, roleId, residenceId, residentialId)
            val response =
                client.post("https://xeejvlwrklfyoinsmmlu.supabase.co/functions/v1/create-user") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

            val text = response.bodyAsText()
            val json = Json.parseToJsonElement(text).jsonObject
            val success = json["success"]?.jsonPrimitive?.booleanOrNull == true

            if (success) {
                null
            } else {
                val errorMsg = json["error"]?.jsonPrimitive?.content ?: "Error desconocido"

                when {
                    errorMsg.contains("email", true) ->
                        R.string.ya_existe_un_usuario_registrado_con_ese_correo_electr_nico

                    errorMsg.contains("Database error creating new user", true) ->
                        R.string.la_c_dula_ingresada_ya_est_registrada_o_hay_un_dato_duplicado

                    else ->
                        R.string.no_se_pudo_crear_el_usuario
                }
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "Error creando el usuario", e)
            "Ocurrió un error inesperado"
        } finally {
            client.close()
        }
    }

}








