package com.example.cuidalink.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.viewmodel.PatientLocationViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay

// Cada cuánto pedimos ubicación (ms). Balance entre frescura y batería.
private const val LOCATION_INTERVAL_MS = 15_000L
private const val LOCATION_MIN_INTERVAL_MS = 10_000L

// Cada cuánto enviamos batería y pasos al cuidador.
private const val METRICS_INTERVAL_MS = 30_000L

/**
 * Efecto invisible que, mientras esté compuesto, pide permiso de ubicación al paciente
 * y sube su GPS a Supabase periódicamente. El cuidador lo recibe por websockets.
 *
 * No dibuja nada: se coloca dentro de una pantalla del paciente (p. ej. el Home).
 */
@SuppressLint("MissingPermission")
@Composable
fun PatientLocationReporter(viewModel: PatientLocationViewModel = viewModel()) {
    val context = LocalContext.current

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(hasLocationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Pide los permisos una sola vez si aún no los tenemos (ubicación + actividad).
    LaunchedEffect(Unit) {
        if (!granted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }
    }

    // Batería y pasos del dispositivo del paciente; se envían periódicamente.
    val battery = rememberBatteryPercent()
    val steps = rememberStepCount()
    val currentBattery by rememberUpdatedState(battery)
    val currentSteps by rememberUpdatedState(steps)
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.reportMetrics(currentBattery, currentSteps)
            delay(METRICS_INTERVAL_MS)
        }
    }

    // Arranca/para las actualizaciones según el permiso; limpia al salir de pantalla.
    DisposableEffect(granted) {
        if (!granted) return@DisposableEffect onDispose { }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MS)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                viewModel.report(location.latitude, location.longitude)
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        onDispose { client.removeLocationUpdates(callback) }
    }
}
