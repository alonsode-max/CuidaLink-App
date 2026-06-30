package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.ui.PatientProfileUi
import com.example.cuidalink.repository.ProfileRepository
import com.example.cuidalink.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Expone el perfil del paciente como StateFlow para las tarjetas Bento. */
class PatientProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState<PatientProfileUi>>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState<PatientProfileUi>> = _state.asStateFlow()

    init {
        loadCurrentPatient()
    }

    /** Carga el perfil del paciente vinculado a la sesión actual. */
    fun loadCurrentPatient() {
        load { repository.getCurrentPatientProfile() }
    }

    /** Carga el perfil de un paciente concreto (vista del cuidador). */
    fun loadPatient(uid: String) {
        load { repository.getPatientProfile(uid) }
    }

    private fun load(source: suspend () -> Result<PatientProfileUi>) {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            _state.value = source().fold(
                onSuccess = { ProfileUiState.Success(it) },
                onFailure = { ProfileUiState.Error(it.message ?: "No se pudo cargar el perfil") }
            )
        }
    }

    /** Actualiza la geovalla del paciente. */
    fun updateGeofence(patientUid: String, lat: Double, lng: Double, radius: Float) {
        viewModelScope.launch {
            // Nota: Podríamos añadir un estado de carga aquí si fuera necesario
            repository.updateGeofence(patientUid, lat, lng, radius)
            loadPatient(patientUid) // Recargar para ver los cambios
        }
    }

    /** Solicita la ubicación actual del paciente. */
    fun requestLocation(patientUid: String) {
        viewModelScope.launch {
            repository.requestPatientLocation(patientUid)
        }
    }
}
