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
            val caretaker = service.fetchLinkedCaretaker(patient.id!!)
            patient.toUi(caretaker)
        }

    override suspend fun getPatientProfile(uid: String): Result<PatientProfileUi> =
        runCatching {
            val patient = service.fetchPatientByUid(uid)
                ?: error("No se encontró el paciente con uid=$uid")
            val caretaker = service.fetchLinkedCaretaker(patient.id!!)
            patient.toUi(caretaker)
        }

    override suspend fun getCurrentCaregiverProfile(): Result<CaregiverProfileUi> =
        runCatching {
            val caretaker = service.fetchCurrentCaretaker()
                ?: error("No hay un cuidador vinculado a la sesión actual")
            val patient = service.fetchLinkedPatient(caretaker.id!!)
            caretaker.toUi(patient)
        }

    override suspend fun getCaregiverProfile(uid: String): Result<CaregiverProfileUi> =
        runCatching {
            val caretaker = service.fetchCaretakerByUid(uid)
                ?: error("No se encontró el cuidador con uid=$uid")
            val patient = service.fetchLinkedPatient(caretaker.id!!)
            caretaker.toUi(patient)
        }

    override suspend fun updateGeofence(patientUid: String, lat: Double, lng: Double, radius: Float): Result<Unit> =
        runCatching {
            service.updateGeofence(patientUid, lat, lng, radius)
        }

    override suspend fun updatePatientLocation(patientUid: String, lat: Double, lng: Double): Result<Unit> =
        runCatching {
            service.updatePatientLocation(patientUid, lat, lng)
        }

    override suspend fun requestPatientLocation(patientUid: String): Result<Unit> =
        runCatching {
            service.requestLocation(patientUid)
        }
}
