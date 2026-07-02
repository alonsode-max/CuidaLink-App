package com.example.cuidalink.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observa en tiempo real la fila del paciente vinculado y avisa al cuidador cuando el
 * paciente SALE de su zona segura (transición dentro -> fuera), igual que el SOS.
 * Expone el nombre y la última ubicación del paciente para la pantalla de alerta.
 */
class CaregiverZoneAlertViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    data class ZoneAlert(
        val patientName: String? = null,
        val lat: Double? = null,
        val lng: Double? = null,
        val alerts: Int = 0
    )

    private val _state = MutableStateFlow(ZoneAlert())
    val state: StateFlow<ZoneAlert> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            val caretaker = service.fetchCurrentCaretaker() ?: return@launch
            val patient = service.fetchLinkedPatient(caretaker.id!!) ?: return@launch
            val id = patient.id ?: return@launch
            // Línea base: el estado actual no debe disparar la alerta al abrir la app.
            var wasOutside = isOutsideZone(patient)
            _state.value = ZoneAlert(patient.name, patient.patientLat, patient.patientLng, 0)

            service.observePatientById(id).collect { updated ->
                val outside = isOutsideZone(updated)
                if (outside && !wasOutside) {
                    _state.value = _state.value.copy(
                        patientName = updated.name,
                        lat = updated.patientLat,
                        lng = updated.patientLng,
                        alerts = _state.value.alerts + 1
                    )
                } else {
                    // Mantener la ubicación al día aunque no haya alerta nueva.
                    _state.value = _state.value.copy(
                        patientName = updated.name,
                        lat = updated.patientLat,
                        lng = updated.patientLng
                    )
                }
                wasOutside = outside
            }
        }
    }

    /**
     * ¿El paciente está fuera de su zona segura? Requiere ubicación + geovalla + radio.
     * Devuelve false si falta cualquier dato (no podemos afirmar que salió).
     */
    private fun isOutsideZone(patient: Patient): Boolean {
        val lat = patient.patientLat ?: return false
        val lng = patient.patientLng ?: return false
        val zones = patient.geofences.filter { it.radius > 0f }
        // Sin zonas configuradas no podemos afirmar que salió.
        if (zones.isEmpty()) return false
        // Está fuera solo si NO está dentro de ninguna de sus zonas seguras.
        val insideAny = zones.any { zone ->
            val result = FloatArray(1)
            Location.distanceBetween(lat, lng, zone.lat, zone.lng, result)
            result[0] <= zone.radius
        }
        return !insideAny
    }
}
