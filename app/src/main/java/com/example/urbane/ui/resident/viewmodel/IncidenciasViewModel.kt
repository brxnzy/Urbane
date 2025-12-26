package com.example.urbane.ui.resident.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.urbane.data.remote.supabase
import java.util.UUID

@Serializable
data class Incidencia(
    val id: Int? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    val title: String = "",
    val description: String = "",
    val status: String? = null,
    val type: String? = null,
    @SerialName("residentId")
    val residentId: String = "",
    @SerialName("residentialId")
    val residentialId: Int = 0,
    @SerialName("incident_images")
    val incidentImages: List<IncidentImage>? = null
)

@Serializable
data class IncidentImage(
    val id: Int? = null,
    @SerialName("incidentId")
    val incidentId: Int? = null,
    @SerialName("imageUrl")
    val imageUrl: String = ""
)

@Serializable
data class NuevaIncidencia(
    val title: String,
    val description: String,
    val status: String,
    @SerialName("residentId")
    val residentId: String,
    @SerialName("residentialId")
    val residentialId: Int,
    val type: String
)

@Serializable
data class NuevaImagenIncidencia(
    @SerialName("incidentId")
    val incidentId: Int,
    @SerialName("imageUrl")
    val imageUrl: String
)

sealed class IncidenciasUiState {
    object Loading : IncidenciasUiState()
    data class Success(val incidencias: List<Incidencia>) : IncidenciasUiState()
    data class Error(val message: String) : IncidenciasUiState()
}

class IncidenciasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<IncidenciasUiState>(IncidenciasUiState.Loading)
    val uiState: StateFlow<IncidenciasUiState> = _uiState.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    init {
        loadIncidencias()
    }

    fun loadIncidencias() {
        viewModelScope.launch {
            _uiState.value = IncidenciasUiState.Loading
            try {
                Log.d("IncidenciasVM", "Cargando incidencias...")

                // Cargar incidencias con sus imágenes relacionadas
                val incidencias = supabase.from("incidents")
                    .select {
                        filter {
                            // Puedes agregar filtros aquí si es necesario
                        }
                    }
                    .decodeList<Incidencia>()

                // Cargar imágenes para cada incidencia
                val incidenciasConImagenes = incidencias.map { incidencia ->
                    if (incidencia.id != null) {
                        try {
                            val imagenes = supabase.from("incident_images")
                                .select {
                                    filter {
                                        eq("incidentId", incidencia.id)
                                    }
                                }
                                .decodeList<IncidentImage>()

                            incidencia.copy(incidentImages = imagenes)
                        } catch (e: Exception) {
                            Log.e("IncidenciasVM", "Error al cargar imágenes: ${e.message}")
                            incidencia
                        }
                    } else {
                        incidencia
                    }
                }

                _uiState.value = IncidenciasUiState.Success(
                    incidenciasConImagenes
                        .filter { it.title.isNotBlank() && it.residentId.isNotBlank() }
                        .map { it.copy(status = it.status ?: "Pendiente") }
                        .sortedByDescending { it.createdAt }
                )
            } catch (e: Exception) {
                Log.e("IncidenciasVM", "Error al cargar incidencias: ${e.message}", e)
                _uiState.value = IncidenciasUiState.Error(
                    e.message ?: "Error al cargar incidencias"
                )
            }
        }
    }

    suspend fun uploadImages(
        imageUris: List<Uri>,
        context: android.content.Context,
        incidentId: Int
    ): List<String> {
        val uploadedUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->
            try {
                // Crear ruta con carpeta por incidencia: incident_123/image_1.jpg
                val fileName = "incident_${incidentId}/image_${index + 1}_${UUID.randomUUID().toString().take(8)}.jpg"
                val bucket = supabase.storage.from("incident-images")

                // Leer el contenido de la imagen
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("No se pudo leer la imagen")
                inputStream.close()

                // Subir la imagen
                bucket.upload(fileName, bytes)

                // Obtener URL pública
                val publicUrl = bucket.publicUrl(fileName)
                uploadedUrls.add(publicUrl)

                // Actualizar progreso
                _uploadProgress.value = (index + 1).toFloat() / imageUris.size

                Log.d("IncidenciasVM", "Imagen subida: $publicUrl")
            } catch (e: Exception) {
                Log.e("IncidenciasVM", "Error al subir imagen: ${e.message}", e)
                throw e
            }
        }

        return uploadedUrls
    }

    fun createIncidencia(
        titulo: String,
        descripcion: String,
        tipo: String?,
        residentId: String,
        residentialId: Int,
        imageUris: List<Uri>,
        context: android.content.Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (titulo.isBlank()) {
            onError("El título es obligatorio")
            return
        }

        if (descripcion.isBlank()) {
            onError("La descripción es obligatoria")
            return
        }

        if (residentId.isBlank()) {
            onError("No se pudo identificar al usuario")
            return
        }

        if (imageUris.isEmpty()) {
            onError("Debes agregar al menos 1 imagen")
            return
        }

        if (imageUris.size > 3) {
            onError("Máximo 3 imágenes permitidas")
            return
        }

        viewModelScope.launch {
            _isCreating.value = true
            _uploadProgress.value = 0f
            try {
                // 1. Crear la incidencia primero
                val nueva = NuevaIncidencia(
                    title = titulo,
                    description = descripcion,
                    status = "Pendiente",
                    residentId = residentId,
                    residentialId = residentialId,
                    type = tipo ?: "Sin especificar"
                )

                Log.d("IncidenciasVM", "Insertando incidencia: $nueva")

                // Insertar y obtener el ID de la incidencia creada
                val incidenciaCreada = supabase.from("incidents")
                    .insert(nueva) {
                        select()
                    }
                    .decodeSingle<Incidencia>()

                val incidenciaId = incidenciaCreada.id ?: throw Exception("No se pudo obtener el ID de la incidencia")

                Log.d("IncidenciasVM", "Incidencia creada con ID: $incidenciaId")

                // 2. Subir imágenes a carpeta específica de la incidencia
                val imageUrls = uploadImages(imageUris, context, incidenciaId)

                // 3. Guardar referencias de imágenes en la tabla incident_images
                imageUrls.forEach { imageUrl ->
                    val nuevaImagen = NuevaImagenIncidencia(
                        incidentId = incidenciaId,
                        imageUrl = imageUrl
                    )

                    supabase.from("incident_images").insert(nuevaImagen)
                    Log.d("IncidenciasVM", "Imagen guardada en BD: $imageUrl")
                }

                Log.d("IncidenciasVM", "✓ Incidencia e imágenes creadas exitosamente")
                loadIncidencias()
                onSuccess()
            } catch (e: Exception) {
                Log.e("IncidenciasVM", "✗ Error: ${e.message}", e)
                onError(e.message ?: "Error al crear la incidencia")
            } finally {
                _isCreating.value = false
                _uploadProgress.value = 0f
            }
        }
    }

    fun updateIncidenciaStatus(incidenciaId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                Log.d("IncidenciasVM", "Actualizando status de incidencia $incidenciaId a $newStatus")
                supabase.from("incidents")
                    .update({
                        set("status", newStatus)
                    }) {
                        filter {
                            eq("id", incidenciaId)
                        }
                    }

                Log.d("IncidenciasVM", "Status actualizado exitosamente")
                loadIncidencias()
            } catch (e: Exception) {
                Log.e("IncidenciasVM", "Error al actualizar status: ${e.message}", e)
                _uiState.value = IncidenciasUiState.Error(
                    e.message ?: "Error al actualizar el estado"
                )
            }
        }
    }

    fun deleteIncidencia(
        incidenciaId: Int,
        residentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("IncidenciasVM", "Eliminando incidencia $incidenciaId")

                // 1. Eliminar toda la carpeta de imágenes del storage
                try {
                    val bucket = supabase.storage.from("incident-images")
                    val folderPath = "incident_$incidenciaId"

                    // Listar todos los archivos en la carpeta
                    val files = bucket.list(folderPath)

                    // Eliminar cada archivo
                    files.forEach { file ->
                        try {
                            bucket.delete("$folderPath/${file.name}")
                            Log.d("IncidenciasVM", "Archivo eliminado: ${file.name}")
                        } catch (e: Exception) {
                            Log.e("IncidenciasVM", "Error al eliminar archivo: ${e.message}")
                        }
                    }

                    Log.d("IncidenciasVM", "Carpeta de imágenes eliminada")
                } catch (e: Exception) {
                    Log.e("IncidenciasVM", "Error al eliminar carpeta de imágenes: ${e.message}")
                    // Continuar con la eliminación de registros aunque falle esto
                }

                // 2. Eliminar registros de incident_images
                supabase.from("incident_images")
                    .delete {
                        filter {
                            eq("incidentId", incidenciaId)
                        }
                    }

                // 3. Eliminar la incidencia
                supabase.from("incidents")
                    .delete {
                        filter {
                            eq("id", incidenciaId)
                            eq("residentId", residentId)
                        }
                    }

                Log.d("IncidenciasVM", "✓ Incidencia eliminada exitosamente")
                loadIncidencias()
                onSuccess()
            } catch (e: Exception) {
                Log.e("IncidenciasVM", "✗ Error al eliminar: ${e.message}", e)
                onError(e.message ?: "Error al eliminar la incidencia")
            }
        }
    }
}