package com.example.cuidalink.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.viewmodel.PatientSosViewModel

/**
 * Efecto invisible y global: escucha si el cuidador activó un SOS para este paciente.
 * Cuando llega, lanza una notificación local y dispara [onSosReceived] (para abrir el
 * modo de auxilio si la app está en primer plano).
 */
@Composable
fun PatientSosWatcher(
    onSosReceived: () -> Unit,
    viewModel: PatientSosViewModel = viewModel()
) {
    val context = LocalContext.current
    val alerts by viewModel.alerts.collectAsState()
    LaunchedEffect(alerts) {
        if (alerts > 0) {
            notifyIncomingSos(context)
            onSosReceived()
        }
    }
}
