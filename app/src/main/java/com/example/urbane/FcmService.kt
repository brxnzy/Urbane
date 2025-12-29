package com.example.urbane
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.urbane.data.remote.supabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FcmService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showNotification(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado: $token")

        CoroutineScope(Dispatchers.IO).launch {
            updateFcmToken(token)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun updateFcmToken(token: String) {
        try {
            val session = supabase.auth.currentSessionOrNull()

            if (session == null) {
                Log.d("FCM", "No hay sesión, guardando token localmente")
                saveTokenLocally(token)
                return
            }

            val userId = session.user?.id ?: return

            // Actualizar el token manteniendo los datos existentes
            supabase.from("user_tokens")
                .update({
                    set("fcm_token", token)
                    set("updated_at", Clock.System.now().toString())
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            Log.d("FCM", "Token actualizado en Supabase")
        } catch (e: Exception) {
            Log.e("FCM", "Error actualizando token: ${e.message}")
            saveTokenLocally(token)
        }
    }

    private fun saveTokenLocally(token: String) {
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("pending_token", token)
            .apply()
    }

    private fun showNotification(message: RemoteMessage) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, Test.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
