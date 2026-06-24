package com.example.cuidalink.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Patient(
    val id: String? = null, // ID autoincremental (bigint)
    val uid: String,        // UID de Supabase (UUID)
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String,
    val age: Int,
    @SerialName("blood_group") val bloodGroup: String,
    val allergies: String,
    val weight: Float,
    val height: Float,
    @SerialName("created_at") val createdAt: String,
    @SerialName("profile_pic") val profilePic: String? = null
)

@Serializable
data class Caretaker(
    val id: String? = null, // ID autoincremental (bigint)
    val uid: String,        // UID de Supabase (UUID)
    val name: String,
    val email: String,
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("profile_pic") val profilePic: String? = null
)
