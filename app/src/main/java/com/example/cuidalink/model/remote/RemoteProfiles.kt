package com.example.cuidalink.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO que refleja la tabla `patients` del backend Supabase del compañero. */
@Serializable
data class Patient(
    val id: String? = null,
    val uid: String,
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String? = null,
    val age: Int,
    @SerialName("blood_group") val bloodGroup: String,
    val allergies: String,
    val weight: Float,
    val height: Float,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profile_pic") val profilePic: String? = null
)

/** DTO que refleja la tabla `caretakers` del backend Supabase del compañero. */
@Serializable
data class Caretaker(
    val id: String? = null,
    val uid: String,
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("profile_pic") val profilePic: String? = null
)
