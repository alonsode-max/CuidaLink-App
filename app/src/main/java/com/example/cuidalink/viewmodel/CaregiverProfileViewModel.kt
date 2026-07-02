package com.example.cuidalink.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.repository.ProfileRepository
import com.example.cuidalink.repository.ProfileRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Expone el perfil del propio cuidador como StateFlow para las tarjetas Bento. */
class CaregiverProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState<CaregiverProfileUi>>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState<CaregiverProfileUi>> = _state.asStateFlow()

    // Suscripción Realtime al paciente vinculado; se cancela al recargar/limpiar.
    private var realtimeJob: Job? = null

    // true cuando el paciente está fuera de su zona segura (para avisar al cuidador).
    private val _outsideZone = MutableStateFlow(false)
    val outsideZone: StateFlow<Boolean> = _outsideZone.asStateFlow()

    init {
        loadCurrentCaregiver()
    }

    /** Carga el perfil del cuidador vinculado a la sesión actual. */
    fun loadCurrentCaregiver() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            val result = repository.getCurrentCaregiverProfile()
            _state.value = result.fold(
                onSuccess = { ProfileUiState.Success(it) },
                onFailure = { ProfileUiState.Error(it.message ?: "No se pudo cargar el perfil") }
            )
            // Si hay paciente vinculado, escuchamos sus cambios en tiempo real.
            result.getOrNull()
                ?.takeIf { it.isLinked && it.patientId != null }
                ?.let { observePatient(it.patientId!!) }
        }
    }

    /**
     * ¿El paciente está fuera de su zona segura? Necesita ubicación + geovalla + radio.
     * Devuelve false si falta cualquiera de los datos (no podemos afirmar que salió).
     */
    private fun isOutsideZone(patient: Patient): Boolean {
        val lat = patient.patientLat ?: return false
        val lng = patient.patientLng ?: return false
        val zoneLat = patient.geofenceLat ?: return false
        val zoneLng = patient.geofenceLng ?: return false
        val radius = patient.geofenceRadius ?: return false
        val result = FloatArray(1)
        Location.distanceBetween(lat, lng, zoneLat, zoneLng, result)
        return result[0] > radius
    }

    /** Escucha los cambios del paciente por websockets y refleja su ubicación en el estado. */
    private fun observePatient(patientId: Long) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            repository.observePatient(patientId).collect { patient ->
                val current = _state.value
                if (current is ProfileUiState.Success) {
                    _state.value = ProfileUiState.Success(
                        current.data.copy(
                            patientName = patient.name,
                            patientEmail = patient.email,
                            patientLat = patient.patientLat,
                            patientLng = patient.patientLng,
                            batteryPercent = patient.batteryPercent,
                            steps = patient.steps,
                            minutesPlayed = patient.minutesPlayed,
                            lastActivity = patient.lastActivity,
                            geofenceLat = patient.geofenceLat,
                            geofenceLng = patient.geofenceLng,
                            geofenceRadius = patient.geofenceRadius
                        )
                    )
                    _outsideZone.value = isOutsideZone(patient)
                }
            }
        }
    }
}
