package com.example.lab_10.workers

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.lab_10.MyApplication
import com.example.lab_10.R
import java.text.SimpleDateFormat
import java.util.*

class TouristLogWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        // Lista de lugares turísticos
        val touristPlaces = listOf(
            "🏔️ Miradores de Yanahuara",
            "⛪ Monasterio de Santa Catalina",
            "🏛️ Plaza de Armas",
            "🦅 Cañón del Colca",
            "🦙 Mundo Alpaca",
            "🌋 Volcán Misti",
            "🍴 Picantería La Cau Cau",
            "🏰 Monasterio de la Recoleta"
        )

        val descriptions = listOf(
            "Vista panorámica del volcán Misti",
            "Arquitectura colonial siglo XVI",
            "Centro histórico de Arequipa",
            "Hogar del cóndor andino",
            "Textiles y camélidos sudamericanos",
            "Volcán activo a 5,822 msnm",
            "Gastronomía arequipeña tradicional",
            "Historia franciscana desde 1648"
        )

        // Obtener un lugar aleatorio
        val randomIndex = touristPlaces.indices.random()
        val place = touristPlaces[randomIndex]
        val description = descriptions[randomIndex]

        // Hora actual
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            .format(Date())

        // Log en consola
        val logMessage = "🏛️ GUÍA TURÍSTICA: $place - $description [${currentTime}]"
        Log.d("TouristGuide", logMessage)
        Log.i("TouristGuide", "Worker ejecutado exitosamente")

        // Mostrar notificación
        showNotification(place, description, currentTime)

        return Result.success()
    }

    private fun showNotification(place: String, description: String, time: String) {
        // Construir la notificación usando el canal creado en MyApplication
        val notification = NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(place)
            .setContentText(description)
            .setSubText("Hora: $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}