package com.example.cuidalink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.viewmodel.CaregiverIncomingSosViewModel

/**
 * Efecto invisible y global: escucha si el paciente vinculado activó su SOS.
 * Cuando llega, lanza una notificación local y dispara [onSosReceived] (para abrir la
 * pantalla de alerta con su ubicación si la app está en primer plano).
 */
@Composable
fun CaregiverSosWatcher(
    onSosReceived: () -> Unit,
    viewModel: CaregiverIncomingSosViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.alerts) {
        if (state.alerts > 0) {
            notifyPatientSos(context, state.patientName ?: "Tu paciente")
            onSosReceived()
        }
    }
}
