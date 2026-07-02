package com.example.cuidalink.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Nivel de batería del dispositivo (0..100), actualizado cuando cambia. */
@Composable
fun rememberBatteryPercent(): Int {
    val context = LocalContext.current
    var level by remember { mutableIntStateOf(currentBatteryPercent(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent ?: return
                val raw = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (raw >= 0 && scale > 0) {
                    level = (raw * 100 / scale).coerceIn(0, 100)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return level
}

private fun currentBatteryPercent(context: Context): Int {
    val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val capacity = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    return if (capacity in 0..100) capacity else 0
}

/**
 * Pasos dados desde que se abrió la pantalla, usando el sensor `TYPE_STEP_COUNTER`.
 *
 * El sensor da el acumulado desde el arranque del móvil, así que tomamos la primera
 * lectura como línea base y devolvemos la diferencia. Devuelve 0 si el dispositivo
 * no tiene sensor de pasos (p. ej. muchos emuladores) o falta el permiso.
 */
@Composable
fun rememberStepCount(): Int {
    val context = LocalContext.current
    var steps by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensorManager == null || stepSensor == null) {
            return@DisposableEffect onDispose { }
        }

        var baseline = -1f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val total = event.values.firstOrNull() ?: return
                if (baseline < 0f) baseline = total
                steps = (total - baseline).toInt().coerceAtLeast(0)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return steps
}
