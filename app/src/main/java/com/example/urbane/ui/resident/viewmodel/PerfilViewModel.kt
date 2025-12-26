// ============ PerfilViewModel.kt ============
package com.example.urbane.ui.resident.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbane.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("photoUrl") val photoUrl: String? = null,
    @SerialName("photo_url") val photoUrlSnake: String? = null,
    @SerialName("idCard") val idCard: String? = null,
    @SerialName("activo") val activo: Boolean? = null
) {
    // Helper para obtener el nombre sin importar el formato
    fun obtenerNombre(): String = nombre ?: name ?: "Usuario"
    fun obtenerPhotoUrl(): String? = photoUrl ?: photoUrlSnake
}

data class PerfilUiState(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class PerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                println("🔍 Cargando perfil para userId: $userId")
                println("📋 Buscando en tabla users...")

                val profile = withContext(Dispatchers.IO) {
                    try {
                        // Método 1: Intentar con filtro directo
                        supabase.from("users")
                            .select {
                                filter {
                                    eq("id", userId)
                                }
                            }
                            .decodeSingle<UserProfile>()
                    } catch (e1: Exception) {
                        println("⚠️ Método 1 falló: ${e1.message}")
                        try {
                            // Método 2: Obtener todos y filtrar
                            val allUsers = supabase.from("users")
                                .select()
                                .decodeList<UserProfile>()

                            println("📊 Total usuarios encontrados: ${allUsers.size}")
                            allUsers.firstOrNull { it.id == userId }
                                ?: throw Exception("Usuario no encontrado con id: $userId")
                        } catch (e2: Exception) {
                            println("⚠️ Método 2 falló: ${e2.message}")
                            throw e2
                        }
                    }
                }

                println("✅ Perfil cargado: id=${profile.id}, nombre=${profile.obtenerNombre()}, email=${profile.email}")

                _uiState.value = _uiState.value.copy(
                    name = profile.obtenerNombre(),
                    email = profile.email ?: "",
                    profileImageUrl = if (!profile.obtenerPhotoUrl().isNullOrBlank()) {
                        "${profile.obtenerPhotoUrl()}?t=${System.currentTimeMillis()}"
                    } else {
                        "${getBucketUrl(userId)}?t=${System.currentTimeMillis()}"
                    },
                    isLoading = false
                )

                println("✅ Estado actualizado: name=${_uiState.value.name}, email=${_uiState.value.email}")

                println("✅ Estado actualizado correctamente")
            } catch (e: Exception) {
                println("❌ Error completo al cargar perfil:")
                println("❌ Mensaje: ${e.message}")
                println("❌ Tipo: ${e::class.simpleName}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo cargar el perfil. Verifica tu conexión."
                )
            }
        }
    }

    fun updateUserName(userId: String, newName: String) {
        if (newName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre es obligatorio"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            try {
                println("💾 Guardando nombre: $newName para userId: $userId")

                withContext(Dispatchers.IO) {
                    // Intentar con "nombre" primero
                    try {
                        supabase.from("users").update(
                            {
                                set("nombre", newName)
                            }
                        ) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        println("✅ Actualizado con campo 'nombre'")
                    } catch (e: Exception) {
                        println("⚠️ Falló con 'nombre', intentando con 'name'")
                        // Si falla, intentar con "name"
                        supabase.from("users").update(
                            {
                                set("name", newName)
                            }
                        ) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        println("✅ Actualizado con campo 'name'")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    name = newName,
                    isSaving = false,
                    successMessage = "Perfil actualizado con éxito"
                )
            } catch (e: Exception) {
                println("❌ Error al guardar: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error al actualizar el perfil: ${e.message}"
                )
            }
        }
    }

    fun uploadProfilePicture(context: Context, userId: String, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)

            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    }
                } ?: throw Exception("No se pudo leer la imagen")

                val fileName = "profile_$userId.jpg"
                val bucket = supabase.storage.from("avatars")

                withContext(Dispatchers.IO) {
                    // Primero intentar eliminar si existe
                    try {
                        bucket.delete(fileName)
                    } catch (e: Exception) {
                        // Ignorar si no existe
                    }
                    // Subir nueva imagen
                    bucket.upload(fileName, bytes)
                }

                // Generar URL con timestamp para evitar cache
                val imageUrl = "${getBucketUrl(userId)}?t=${System.currentTimeMillis()}"

                withContext(Dispatchers.IO) {
                    // Intentar con ambos nombres de columna
                    try {
                        supabase.from("users").update(
                            {
                                set("photoUrl", imageUrl)
                            }
                        ) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        println("✅ Foto actualizada con campo 'photoUrl'")
                    } catch (e: Exception) {
                        println("⚠️ Falló con 'photoUrl', intentando con 'photo_url'")
                        supabase.from("users").update(
                            {
                                set("photo_url", imageUrl)
                            }
                        ) {
                            filter {
                                eq("id", userId)
                            }
                        }
                        println("✅ Foto actualizada con campo 'photo_url'")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    profileImageUrl = imageUrl,
                    isUploading = false,
                    successMessage = "¡Foto actualizada con éxito!"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = "Error al subir la foto: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    private fun getBucketUrl(userId: String): String {
        return supabase.storage.from("avatars").publicUrl("profile_$userId.jpg")
    }
}
