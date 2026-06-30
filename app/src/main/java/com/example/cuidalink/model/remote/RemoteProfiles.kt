package com.example.cuidalink.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO que refleja la tabla `patients` del backend Supabase. */
@Serializable
data class Patient(
    val id: Long? = null,
    val uid: String,
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String? = null,
    val age: Int,
    @SerialName("blood_group") val bloodGroup: String? = null,
    val allergies: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    /** Código único de 6 dígitos para vincular cuidadores. */
    val code: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profile_pic") val profilePic: String? = null,
    
    // Ubicación y geovalla
    @SerialName("patient_lat") val patientLat: Double? = null,
    @SerialName("patient_lng") val patientLng: Double? = null,
    @SerialName("geofence_lat") val geofenceLat: Double? = null,
    @SerialName("geofence_lng") val geofenceLng: Double? = null,
    @SerialName("geofence_radius") val geofenceRadius: Float? = null,
    
    // Campo para solicitar ubicación (ping)
    @SerialName("location_request_trigger") val locationRequestTrigger: String? = null
)

/** DTO de la tabla `vinculations`. */
@Serializable
data class Vinculation(
    val id: Long? = null,
    @SerialName("patient_id") val patientId: Long,
    @SerialName("caretaker_id") val caretakerId: Long,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Payload de inserción para `vinculations`: SOLO las columnas que escribimos.
 *
 * Evita enviar `id` (identity) o `created_at` (NOT NULL con default `now()`),
 * que Postgres rechaza si llegan como `null`. Dejamos que la DB los rellene.
 */
@Serializable
data class VinculationInsert(
    @SerialName("patient_id") val patientId: Long,
    @SerialName("caretaker_id") val caretakerId: Long
)

/** DTO que refleja la tabla `caretakers` del backend Supabase. */
@Serializable
data class Caretaker(
    val id: Long? = null,
    val uid: String,
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profile_pic") val profilePic: String? = null
)
