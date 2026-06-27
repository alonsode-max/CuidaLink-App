package com.example.cuidalink.model.ui

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.Patient

/**
 * Modelo que alimenta las tarjetas Bento del perfil del paciente.
 *
 * Los campos sin respaldo aún en el esquema del backend (dirección, diagnóstico,
 * etapa, medicación, médico, contacto de emergencia) quedan nullable para que la
 * UI muestre un fallback hasta que el compañero añada esas tablas/columnas.
 */
data class PatientProfileUi(
    val name: String,
    val email: String?,
    val age: Int?,
    val bloodGroup: String?,
    val allergies: String?,
    val weightKg: Float?,
    val heightM: Float?,
    val profilePicUrl: String?,
    val address: String? = null,
    val diagnosis: String? = null,
    val stage: String? = null,
    val medication: String? = null,
    val doctorName: String? = null,
    val doctorPhone: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactPhone: String? = null
)

/**
 * Modelo que alimenta las tarjetas Bento del perfil del propio cuidador.
 *
 * `relationship`, `phone` y los datos del paciente a cargo aún no existen en la
 * tabla `caretakers`; quedan nullable como costura para el backend futuro.
 */
data class CaregiverProfileUi(
    val name: String,
    val email: String?,
    val profilePicUrl: String?,
    val relationship: String? = null,
    val phone: String? = null,
    val patientName: String? = null,
    val patientRelation: String? = null,
    val patientDeviceId: String? = null
)

/** Mapea la fila `patients` del backend al modelo de las tarjetas Bento. */
fun Patient.toUi(): PatientProfileUi = PatientProfileUi(
    name = name,
    email = email,
    age = age,
    bloodGroup = bloodGroup,
    allergies = allergies,
    weightKg = weight,
    heightM = height,
    profilePicUrl = profilePic
)

/** Mapea la fila `caretakers` del backend al modelo de las tarjetas Bento. */
fun Caretaker.toUi(): CaregiverProfileUi = CaregiverProfileUi(
    name = name,
    email = email,
    profilePicUrl = profilePic
)
