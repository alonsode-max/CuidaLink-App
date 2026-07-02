package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.repository.ProfileRepository
import com.example.cuidalink.repository.ProfileRepositoryImpl
import kotlinx.coroutines.launch

/**
 * Envía la ubicación del paciente autenticado a Supabase (columnas `patient_lat` /
 * `patient_lng`), de donde el cuidador la recibe por websockets en tiempo real.
 *
 * Es "best-effort": si no hay sesión o la red falla, se ignora en silencio para no
 * interrumpir la experiencia del paciente.
 */
class PatientLocationViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl(),
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    /** Sube la última posición conocida del paciente. */
    fun report(lat: Double, lng: Double) {
        val uid = service.currentUid() ?: return
        viewModelScope.launch {
            repository.updatePatientLocation(uid, lat, lng)
        }
    }

    /** Sube batería y pasos del paciente. */
    fun reportMetrics(batteryPercent: Int, steps: Int) {
        val uid = service.currentUid() ?: return
        viewModelScope.launch {
            repository.updatePatientMetrics(uid, batteryPercent, steps)
        }
    }

    /** Registra una sesión de juego terminada (suma minutos + última actividad). */
    fun reportGameActivity(minutesToAdd: Int, activity: String) {
        val uid = service.currentUid() ?: return
        viewModelScope.launch {
            repository.addGameActivity(uid, minutesToAdd, activity)
        }
    }
}
