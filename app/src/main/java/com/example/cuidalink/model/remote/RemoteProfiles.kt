package com.example.cuidalink.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO que refleja la tabla `patients` del backend Supabase del compañero. */
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
    @SerialName("profile_pic") val profilePic: String? = null
)

/**
 * DTO de la tabla `vinculations`: une un paciente con un cuidador.
 *
 * `patientId`/`caretakerId` referencian el `id` (PK) de cada tabla.
 */
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

/** DTO que refleja la tabla `caretakers` del backend Supabase del compañero. */
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
