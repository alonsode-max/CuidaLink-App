package com.example.cuidalink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.viewmodel.PatientLocationViewModel
import kotlinx.coroutines.delay

// Un minuto en milisegundos.
private const val ONE_MINUTE_MS = 60_000L

/**
 * Efecto invisible para pantallas de juego del paciente: marca [activity] como la
 * última actividad y suma 1 minuto jugado por cada minuto que se permanece en la
 * pantalla. El cuidador lo ve en su dashboard (minutos jugados / última actividad).
 *
 * Reporta durante la composición (no en onDispose) para no perder el envío cuando
 * el `viewModelScope` de la pantalla se cancela al salir.
 */
@Composable
fun GameActivityReporter(
    activity: String,
    viewModel: PatientLocationViewModel = viewModel()
) {
    LaunchedEffect(activity) {
        // Marca la actividad de inmediato (suma 0 minutos).
        viewModel.reportGameActivity(0, activity)
        while (true) {
            delay(ONE_MINUTE_MS)
            viewModel.reportGameActivity(1, activity)
        }
    }
}
