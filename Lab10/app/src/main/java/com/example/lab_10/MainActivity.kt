package com.example.lab_10

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.lab_10.ui.theme.Lab10Theme
import com.example.lab_10.workers.TouristLogWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private var permissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(this, "✅ Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "⚠️ Permiso denegado", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkNotificationPermission()

        setContent {
            Lab10Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TouristGuideApp(
                        modifier = Modifier.padding(innerPadding),
                        onStartWorker = { scheduleLogWorker() },
                        onStopWorker = { cancelLogWorker() },
                        onRunOnce = { runWorkerOnce() },
                        onRequestPermission = { requestNotificationPermission() },
                        hasPermission = permissionGranted
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    permissionGranted = true
                    Toast.makeText(this, "✅ Ya tienes permiso", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            permissionGranted = true
            Toast.makeText(this, "✅ No requiere permiso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleLogWorker() {
        val workRequest = PeriodicWorkRequestBuilder<TouristLogWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TouristLogWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Toast.makeText(this, "🔄 Worker programado", Toast.LENGTH_SHORT).show()
    }

    private fun runWorkerOnce() {
        val workRequest = OneTimeWorkRequestBuilder<TouristLogWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
        Toast.makeText(this, "⚡ Ejecutando en 1 segundo...", Toast.LENGTH_SHORT).show()
    }

    private fun cancelLogWorker() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork("TouristLogWork")
        Toast.makeText(this, "⏹️ Worker detenido", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun TouristGuideApp(
    modifier: Modifier = Modifier,
    onStartWorker: () -> Unit,
    onStopWorker: () -> Unit,
    onRunOnce: () -> Unit,
    onRequestPermission: () -> Unit,
    hasPermission: Boolean
) {
    var workerStatus by remember { mutableStateOf("Listo para usar") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🏛️ Guía Turística",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Arequipa - La Ciudad Blanca",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Estado del permiso
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasPermission)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (hasPermission) "✅ Notificaciones Habilitadas" else "⚠️ Permiso Requerido",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasPermission)
                        "Recibirás notificaciones de lugares turísticos"
                    else
                        "Otorga el permiso para recibir notificaciones",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Botón de permiso
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(
                onClick = { onRequestPermission() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🔔 Solicitar Permiso")
            }
        }

        Divider()

        // Botones de control
        Button(
            onClick = {
                onRunOnce()
                workerStatus = "⚡ Ejecutando..."
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("⚡ Mostrar Notificación Ahora")
        }

        Button(
            onClick = {
                onStartWorker()
                workerStatus = "🔄 Programado cada 15 min"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("▶️ Iniciar Notificaciones Periódicas")
        }

        OutlinedButton(
            onClick = {
                onStopWorker()
                workerStatus = "⏹️ Detenido"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⏹️ Detener")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Estado: $workerStatus",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "💡 Desliza desde arriba para ver notificaciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}