package com.example.cuidalink.repository

import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.model.ui.PatientProfileUi
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a los perfiles del backend ya mapeados a los modelos de las tarjetas Bento.
 *
 * Todas las operaciones son suspendidas y devuelven `Result` para que la UI maneje
 * el error de forma explícita (sin try/catch en las pantallas).
 */
interface ProfileRepository {

    /** Perfil del paciente de la sesión actual (pantalla del paciente). */
    suspend fun getCurrentPatientProfile(): Result<PatientProfileUi>

    /** Perfil de un paciente concreto por su `uid` (vista del cuidador). */
    suspend fun getPatientProfile(uid: String): Result<PatientProfileUi>

    /** Perfil del paciente VINCULADO al cuidador de la sesión actual. */
    suspend fun getLinkedPatientProfile(): Result<PatientProfileUi>

    /** Perfil del propio cuidador de la sesión actual ("Mi perfil"). */
    suspend fun getCurrentCaregiverProfile(): Result<CaregiverProfileUi>

    /** Perfil de un cuidador concreto por su `uid`. */
    suspend fun getCaregiverProfile(uid: String): Result<CaregiverProfileUi>

    /** Actualiza la geovalla del paciente. */
    suspend fun updateGeofence(patientUid: String, lat: Double, lng: Double, radius: Float): Result<Unit>
    
    /** Actualiza la ubicación del paciente. */
    suspend fun updatePatientLocation(patientUid: String, lat: Double, lng: Double): Result<Unit>

    /** Solicita la ubicación del paciente (vía flag en DB o FCM). */
    suspend fun requestPatientLocation(patientUid: String): Result<Unit>

    /** Flujo en tiempo real (websockets) con los cambios de la fila del paciente. */
    fun observePatient(patientId: Long): Flow<Patient>

    /** Envía batería y pasos del paciente al backend. */
    suspend fun updatePatientMetrics(patientUid: String, batteryPercent: Int, steps: Int): Result<Unit>

    /** Suma minutos jugados y fija la última actividad del paciente. */
    suspend fun addGameActivity(patientUid: String, minutesToAdd: Int, activity: String): Result<Unit>
}
