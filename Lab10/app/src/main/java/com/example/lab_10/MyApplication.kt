package com.example.lab_10

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApplication : Application() {

    companion object {
        const val CHANNEL_ID = "tourist_guide_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Solo necesario para Android 8.0 (API 26) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Guía Turística"
            val descriptionText = "Notificaciones de lugares turísticos de Arequipa"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}