package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SOS del cuidador: al entrar, avisa al paciente (escribe `sos_alert_trigger`, que la
 * app del paciente observa) y expone su nombre y ubicación para mostrarlos.
 *
 * Usa una lectura puntual (no Realtime) para no abrir un segundo canal con el mismo
 * id que el del dashboard, lo que hacía crashear la app.
 */
class CaregiverSosViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    data class SosUi(
        val patientName: String? = null,
        val lat: Double? = null,
        val lng: Double? = null,
        val sent: Boolean = false
    )

    private val _state = MutableStateFlow(SosUi())
    val state: StateFlow<SosUi> = _state.asStateFlow()

    init {
        activate()
    }

    private fun activate() {
        viewModelScope.launch {
            val caretaker = service.fetchCurrentCaretaker() ?: return@launch
            val patient = service.fetchLinkedPatient(caretaker.id!!) ?: return@launch
            _state.value = SosUi(
                patientName = patient.name,
                lat = patient.patientLat,
                lng = patient.patientLng,
                sent = false
            )
            runCatching { service.triggerSosAlert(patient.uid) }
            _state.value = _state.value.copy(sent = true)
        }
    }
}
