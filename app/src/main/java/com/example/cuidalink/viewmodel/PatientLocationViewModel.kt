package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.repository.ProfileRepository
import com.example.cuidalink.repository.ProfileRepositoryImpl
import kotlinx.coroutines.launch

// Intervalo mínimo entre puntos guardados en el historial de ubicación (ms).
private const val HISTORY_MIN_INTERVAL_MS = 60_000L

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

    // Id del paciente cacheado para no resolverlo en cada reporte.
    private var patientId: Long? = null
    // Momento del último punto guardado en el historial (para no saturarlo).
    private var lastHistoryAt = 0L

    /** Sube la última posición conocida del paciente y guarda un punto en el historial. */
    fun report(lat: Double, lng: Double) {
        val uid = service.currentUid() ?: return
        viewModelScope.launch {
            repository.updatePatientLocation(uid, lat, lng)

            // Historial: como máximo un punto cada HISTORY_MIN_INTERVAL_MS.
            val now = System.currentTimeMillis()
            if (now - lastHistoryAt < HISTORY_MIN_INTERVAL_MS) return@launch
            val id = patientId ?: service.fetchCurrentPatient()?.id?.also { patientId = it } ?: return@launch
            runCatching { service.insertLocationHistory(id, lat, lng) }
                .onSuccess { lastHistoryAt = now }
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
