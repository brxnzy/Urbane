package com.example.urbane

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging

class Test : Application() {
    companion object{
        const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
    }
    override fun onCreate() {
        super.onCreate()
        Firebase.messaging.token.addOnCompleteListener {
            if (!it.isSuccessful) {
                println("Token no generado")
                return@addOnCompleteListener
            }
            val token = it.result
            println("El token es $token")
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificaciones de Prueba",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Estas notificaciones seran recibidas desde fcm"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

        }

    }
}