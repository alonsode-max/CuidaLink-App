package com.example.cuidalink.repository

import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.model.ui.PatientProfileUi

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

    /** Perfil del propio cuidador de la sesión actual ("Mi perfil"). */
    suspend fun getCurrentCaregiverProfile(): Result<CaregiverProfileUi>

    /** Perfil de un cuidador concreto por su `uid`. */
    suspend fun getCaregiverProfile(uid: String): Result<CaregiverProfileUi>
}
