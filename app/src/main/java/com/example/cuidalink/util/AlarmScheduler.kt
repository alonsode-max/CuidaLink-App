package com.example.cuidalink.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.cuidalink.model.Event
import com.example.cuidalink.receiver.AlarmReceiver
import java.util.Calendar

fun scheduleAlarms(context: Context, event: Event) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("EVENT_NAME", event.name)
    }

    if (event.isRecurring) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, event.time.hour)
            set(Calendar.MINUTE, event.time.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    } else {
        event.dates.forEach { date ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, event.time.hour)
                set(Calendar.MINUTE, event.time.minute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.after(Calendar.getInstance())) {
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (event.id + date.toString()).hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }
}
