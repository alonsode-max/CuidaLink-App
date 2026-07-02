package com.example.cuidalink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.viewmodel.CaregiverZoneAlertViewModel

/**
 * Efecto invisible y global: escucha si el paciente vinculado salió de su zona segura.
 * Cuando ocurre, lanza una notificación local y dispara [onZoneExit] (para abrir la
 * pantalla de alerta con su ubicación si la app está en primer plano), igual que el SOS.
 */
@Composable
fun CaregiverZoneWatcher(
    onZoneExit: () -> Unit,
    viewModel: CaregiverZoneAlertViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.alerts) {
        if (state.alerts > 0) {
            notifyZoneExit(context, state.patientName ?: "Tu paciente")
            onZoneExit()
        }
    }
}
