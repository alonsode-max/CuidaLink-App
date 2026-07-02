package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observa en tiempo real la fila del paciente vinculado y avisa al cuidador cuando el
 * paciente activa su SOS (cambia `patient_sos_trigger`). Expone el nombre y la última
 * ubicación del paciente para mostrarlos en la pantalla de alerta.
 */
class CaregiverIncomingSosViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    data class IncomingSos(
        val patientName: String? = null,
        val lat: Double? = null,
        val lng: Double? = null,
        val alerts: Int = 0
    )

    private val _state = MutableStateFlow(IncomingSos())
    val state: StateFlow<IncomingSos> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            val caretaker = service.fetchCurrentCaretaker() ?: return@launch
            val patient = service.fetchLinkedPatient(caretaker.id!!) ?: return@launch
            val id = patient.id ?: return@launch
            // Línea base: el valor actual no debe disparar la alerta.
            var lastTrigger = patient.patientSosTrigger
            _state.value = IncomingSos(
                patientName = patient.name,
                lat = patient.patientLat,
                lng = patient.patientLng,
                alerts = 0
            )
            service.observePatientById(id).collect { updated ->
                if (updated.patientSosTrigger != lastTrigger) {
                    lastTrigger = updated.patientSosTrigger
                    if (updated.patientSosTrigger != null) {
                        _state.value = _state.value.copy(
                            patientName = updated.name,
                            lat = updated.patientLat,
                            lng = updated.patientLng,
                            alerts = _state.value.alerts + 1
                        )
                    }
                } else {
                    // Mantener la ubicación al día aunque no haya alerta nueva.
                    _state.value = _state.value.copy(
                        lat = updated.patientLat,
                        lng = updated.patientLng
                    )
                }
            }
        }
    }
}
