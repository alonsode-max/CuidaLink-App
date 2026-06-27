package com.example.cuidalink.repository

import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.model.ui.PatientProfileUi
import com.example.cuidalink.model.ui.toUi
import com.example.cuidalink.network.ProfileService

/** Implementación que consulta Supabase vía [ProfileService] y mapea a modelos Bento. */
class ProfileRepositoryImpl(
    private val service: ProfileService = ProfileService()
) : ProfileRepository {

    override suspend fun getCurrentPatientProfile(): Result<PatientProfileUi> =
        runCatching {
            val patient = service.fetchCurrentPatient()
                ?: error("No hay un paciente vinculado a la sesión actual")
            patient.toUi()
        }

    override suspend fun getPatientProfile(uid: String): Result<PatientProfileUi> =
        runCatching {
            val patient = service.fetchPatientByUid(uid)
                ?: error("No se encontró el paciente con uid=$uid")
            patient.toUi()
        }

    override suspend fun getCurrentCaregiverProfile(): Result<CaregiverProfileUi> =
        runCatching {
            val caretaker = service.fetchCurrentCaretaker()
                ?: error("No hay un cuidador vinculado a la sesión actual")
            caretaker.toUi()
        }

    override suspend fun getCaregiverProfile(uid: String): Result<CaregiverProfileUi> =
        runCatching {
            val caretaker = service.fetchCaretakerByUid(uid)
                ?: error("No se encontró el cuidador con uid=$uid")
            caretaker.toUi()
        }
}
