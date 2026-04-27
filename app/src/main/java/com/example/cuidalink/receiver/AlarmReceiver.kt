package com.example.cuidalink.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("EVENT_NAME") ?: "Evento"
        Log.d("AlarmReceiver", "Iniciando servicio de alarma para: $eventName")
        
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("EVENT_NAME", eventName)
        }
        
        // Iniciamos el servicio en primer plano (Foreground Service)
        context.startForegroundService(serviceIntent)
    }
}
