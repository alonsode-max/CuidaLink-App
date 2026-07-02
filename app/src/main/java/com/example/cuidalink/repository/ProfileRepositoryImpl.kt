package com.example.cuidalink.repository

import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.model.ui.PatientProfileUi
import com.example.cuidalink.model.ui.toUi
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.Flow

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

    override suspend fun getLinkedPatientProfile(): Result<PatientProfileUi> =
        runCatching {
            val caretaker = service.fetchCurrentCaretaker()
                ?: error("No hay un cuidador en la sesión actual")
            val patient = service.fetchLinkedPatient(caretaker.id!!)
                ?: error("No tienes un paciente vinculado")
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

    override suspend fun clearGeofence(patientUid: String): Result<Unit> =
        runCatching { service.clearGeofence(patientUid) }

    override suspend fun setGeofences(
        patientUid: String,
        zones: List<com.example.cuidalink.model.remote.GeofenceZone>
    ): Result<Unit> =
        runCatching { service.setGeofences(patientUid, zones) }

    override suspend fun updatePatientLocation(patientUid: String, lat: Double, lng: Double): Result<Unit> =
        runCatching {
            service.updatePatientLocation(patientUid, lat, lng)
        }

    override suspend fun requestPatientLocation(patientUid: String): Result<Unit> =
        runCatching {
            service.requestLocation(patientUid)
        }

    override fun observePatient(patientId: Long): Flow<Patient> =
        service.observePatientById(patientId)

    override suspend fun updatePatientMetrics(patientUid: String, batteryPercent: Int, steps: Int): Result<Unit> =
        runCatching { service.updatePatientMetrics(patientUid, batteryPercent, steps) }

    override suspend fun addGameActivity(patientUid: String, minutesToAdd: Int, activity: String): Result<Unit> =
        runCatching { service.addGameActivity(patientUid, minutesToAdd, activity) }

    override suspend fun uploadProfilePhoto(bytes: ByteArray): Result<String> =
        runCatching {
            service.uploadProfilePhoto(bytes)
                ?: error("No hay una sesión activa para subir la foto")
        }
}
