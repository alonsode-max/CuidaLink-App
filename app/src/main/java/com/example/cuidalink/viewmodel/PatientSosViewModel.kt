package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observa la fila del paciente en tiempo real y avisa cuando el cuidador dispara un
 * SOS (cambia `sos_alert_trigger`). El contador se incrementa en cada alerta nueva.
 */
class PatientSosViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    private val _alerts = MutableStateFlow(0)
    val alerts: StateFlow<Int> = _alerts.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            val patient = service.fetchCurrentPatient()
            val id = patient?.id ?: return@launch
            // Línea base: el valor actual no debe disparar la alerta.
            var lastTrigger = patient.sosAlertTrigger
            service.observePatientById(id).collect { updated ->
                if (updated.sosAlertTrigger != lastTrigger) {
                    lastTrigger = updated.sosAlertTrigger
                    if (updated.sosAlertTrigger != null) {
                        _alerts.value = _alerts.value + 1
                    }
                }
            }
        }
    }
}
