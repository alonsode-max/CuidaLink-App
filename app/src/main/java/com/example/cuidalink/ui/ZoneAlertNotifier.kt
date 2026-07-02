package com.example.cuidalink.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cuidalink.R

private const val ZONE_CHANNEL_ID = "zona_segura"
private const val ZONE_NOTIFICATION_ID = 4021
private const val SOS_NOTIFICATION_ID = 4022
private const val PATIENT_SOS_NOTIFICATION_ID = 4023

/**
 * Avisa al cuidador de que el paciente ha salido de su zona segura.
 * Es best-effort: si falta el permiso de notificaciones, no lanza nada (el
 * dashboard también muestra un banner visible como respaldo).
 */
fun notifyZoneExit(context: Context, patientName: String) {
    ensureChannel(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, ZONE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Alerta de zona segura")
        .setContentText("$patientName ha salido de su zona segura.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    runCatching {
        NotificationManagerCompat.from(context).notify(ZONE_NOTIFICATION_ID, notification)
    }
}

/**
 * Avisa al paciente de que su cuidador ha activado un SOS para él.
 */
fun notifyIncomingSos(context: Context) {
    ensureChannel(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, ZONE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Tu cuidador activó el SOS")
        .setContentText("Abre CuidaLink: hay una alerta de emergencia para ti.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    runCatching {
        NotificationManagerCompat.from(context).notify(SOS_NOTIFICATION_ID, notification)
    }
}

/**
 * Avisa al CUIDADOR de que su paciente ha activado el SOS.
 */
fun notifyPatientSos(context: Context, patientName: String) {
    ensureChannel(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, ZONE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("$patientName activó el SOS")
        .setContentText("Abre CuidaLink: tu paciente necesita ayuda.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    runCatching {
        NotificationManagerCompat.from(context).notify(PATIENT_SOS_NOTIFICATION_ID, notification)
    }
}

private fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (manager.getNotificationChannel(ZONE_CHANNEL_ID) == null) {
        manager.createNotificationChannel(
            NotificationChannel(
                ZONE_CHANNEL_ID,
                "Zona segura",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos cuando el paciente sale de su zona segura" }
        )
    }
}
