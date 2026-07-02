package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SOS activado por el propio PACIENTE. Al llamar [activateSos] escribe
 * `patient_sos_trigger` en su fila para que el cuidador vinculado lo reciba.
 * También expone el nombre del cuidador vinculado y la última ubicación del
 * paciente para mostrarlos en la pantalla de auxilio.
 */
class PatientEmergencyViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    data class EmergencyUi(
        val caretakerName: String? = null,
        val lat: Double? = null,
        val lng: Double? = null
    )

    private val _state = MutableStateFlow(EmergencyUi())
    val state: StateFlow<EmergencyUi> = _state.asStateFlow()

    private var loaded = false

    /** Carga el cuidador vinculado y la ubicación del paciente. Idempotente. */
    fun load() {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            val patient = service.fetchCurrentPatient() ?: return@launch
            val caretaker = patient.id?.let { service.fetchLinkedCaretaker(it) }
            _state.value = EmergencyUi(
                caretakerName = caretaker?.name,
                lat = patient.patientLat,
                lng = patient.patientLng
            )
        }
    }

    /** Avisa al cuidador de que el paciente ha activado la alarma. Best-effort. */
    fun activateSos() {
        viewModelScope.launch {
            runCatching { service.triggerPatientSos() }
        }
    }
}
