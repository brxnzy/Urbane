package com.example.urbane
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.remote.supabase
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class App : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Token no generado: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println("El token es $token")

            applicationScope.launch {
                saveTokenToSupabase(token)
            }
        }

        createNotificationChannel()
    }

    private suspend fun saveTokenToSupabase(token: String) {
        val context = applicationContext
        val sessionManager = SessionManager(context)
        val session = sessionManager.sessionFlow.firstOrNull()
        val userId = session?.userId ?: return
        val residentialId = session.userData!!.residential.id
        val role = session.userData.role.name

        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.from("user_tokens")
                    .upsert(mapOf(
                        "user_id" to userId,
                        "residential_id" to residentialId ,
                        "role" to role.lowercase(),
                        "fcm_token" to token,
                        "updated_at" to "NOW()"
                    ))
                println("Token guardado en Supabase")
            } catch (e: Exception) {
                println("Error guardando token: ${e.message}")
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificaciones de Incidencias",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notificaciones de nuevas incidencias"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}