package com.example.cuidalink.model.ui

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.GeofenceZone
import com.example.cuidalink.model.remote.Patient

/**
 * Modelo que alimenta las tarjetas Bento del perfil del paciente.
 *
 * Los campos sin respaldo aún en el esquema del backend (dirección, diagnóstico,
 * etapa, medicación, médico, contacto de emergencia) quedan nullable para que la
 * UI muestre un fallback hasta que el compañero añada esas tablas/columnas.
 */
data class PatientProfileUi(
    val id: Long? = null,
    val uid: String,
    val name: String,
    val email: String?,
    val age: Int?,
    val bloodGroup: String?,
    val allergies: String?,
    val weightKg: Float?,
    val heightM: Float?,
    val profilePicUrl: String?,
    val geofences: List<GeofenceZone> = emptyList(),
    val address: String? = null,
    val diagnosis: String? = null,
    val stage: String? = null,
    val medication: String? = null,
    val doctorName: String? = null,
    val doctorPhone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactPhone: String? = null,
    // Información del cuidador vinculado
    val caretakerName: String? = null,
    val caretakerEmail: String? = null,
    val isLinked: Boolean = false,
    
    // Ubicación y Geovalla
    val patientLat: Double? = null,
    val patientLng: Double? = null,
    val geofenceLat: Double? = null,
    val geofenceLng: Double? = null,
    val geofenceRadius: Float? = null
)

/**
 * Modelo que alimenta las tarjetas Bento del perfil del propio cuidador.
 */
data class CaregiverProfileUi(
    val name: String,
    val email: String?,
    val profilePicUrl: String?,
    val hasSafeZone: Boolean = false,
    val relationship: String? = null,
    val phone: String? = null,
    val patientId: Long? = null,
    val patientName: String? = null,
    val patientEmail: String? = null,
    val isLinked: Boolean = false,
    // Ubicación del paciente a cargo (para el mapa del dashboard del cuidador).
    val patientLat: Double? = null,
    val patientLng: Double? = null,
    // Telemetría del paciente que se muestra en el dashboard del cuidador.
    val batteryPercent: Int? = null,
    val steps: Int? = null,
    val minutesPlayed: Int? = null,
    val lastActivity: String? = null,
    // Zona segura (geovalla) para avisar si el paciente sale de ella.
    val geofenceLat: Double? = null,
    val geofenceLng: Double? = null,
    val geofenceRadius: Float? = null
)

/** Mapea la fila `patients` del backend al modelo de las tarjetas Bento. */
fun Patient.toUi(caretaker: Caretaker? = null): PatientProfileUi = PatientProfileUi(
    id = id,
    uid = uid,
    name = name,
    email = email,
    age = age,
    bloodGroup = bloodGroup,
    allergies = allergies,
    weightKg = weight,
    heightM = height,
    profilePicUrl = profilePic,
    geofences = geofences,
    caretakerName = caretaker?.name,
    caretakerEmail = caretaker?.email,
    isLinked = caretaker != null,
    patientLat = patientLat,
    patientLng = patientLng,
    geofenceLat = geofenceLat,
    geofenceLng = geofenceLng,
    geofenceRadius = geofenceRadius
)

/** Mapea la fila `caretakers` del backend al modelo de las tarjetas Bento. */
fun Caretaker.toUi(patient: Patient? = null): CaregiverProfileUi = CaregiverProfileUi(
    name = name,
    email = email,
    profilePicUrl = profilePic,
    hasSafeZone = patient?.geofences?.isNotEmpty() == true,
    patientId = patient?.id,
    patientName = patient?.name,
    patientEmail = patient?.email,
    isLinked = patient != null,
    patientLat = patient?.patientLat,
    patientLng = patient?.patientLng,
    batteryPercent = patient?.batteryPercent,
    steps = patient?.steps,
    minutesPlayed = patient?.minutesPlayed,
    lastActivity = patient?.lastActivity,
    geofenceLat = patient?.geofenceLat,
    geofenceLng = patient?.geofenceLng,
    geofenceRadius = patient?.geofenceRadius
)
