package com.example.cuidalink.network

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.remote.Vinculation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID

/**
 * Cliente de API contra las tablas Postgrest del backend (`patients`, `caretakers`).
 *
 * Cada función es una llamada de red suspendida; lanza si Supabase falla y devuelve
 * `null` cuando no existe la fila. El repositorio se encarga de envolverlo en Result.
 */
class ProfileService(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    /** UID del usuario autenticado en Supabase, o null si no hay sesión. */
    fun currentUid(): String? = client.auth.currentUserOrNull()?.id

    /** Lee la fila de `patients` cuyo `uid` coincide con el indicado. */
    suspend fun fetchPatientByUid(uid: String): Patient? =
        client.from(TABLE_PATIENTS)
            .select { filter { eq(COLUMN_UID, uid) } }
            .decodeSingleOrNull()

    /** Lee la fila de `caretakers` cuyo `uid` coincide con el indicado. */
    suspend fun fetchCaretakerByUid(uid: String): Caretaker? =
        client.from(TABLE_CARETAKERS)
            .select { filter { eq(COLUMN_UID, uid) } }
            .decodeSingleOrNull()

    /** Perfil del paciente vinculado a la sesión actual. */
    suspend fun fetchCurrentPatient(): Patient? =
        currentUid()?.let { fetchPatientByUid(it) }

    /** Perfil del cuidador vinculado a la sesión actual. */
    suspend fun fetchCurrentCaretaker(): Caretaker? =
        currentUid()?.let { fetchCaretakerByUid(it) }

    /** Obtiene el cuidador vinculado a un paciente. */
    suspend fun fetchLinkedCaretaker(patientId: Long): Caretaker? {
        val vinculation = client.from(TABLE_VINCULATIONS)
            .select { filter { eq(COLUMN_PATIENT_ID, patientId) } }
            .decodeSingleOrNull<Vinculation>() ?: return null
        
        return client.from(TABLE_CARETAKERS)
            .select { filter { eq("id", vinculation.caretakerId) } }
            .decodeSingleOrNull()
    }

    /** Obtiene el paciente vinculado a un cuidador. */
    suspend fun fetchLinkedPatient(caretakerId: Long): Patient? {
        val vinculation = client.from(TABLE_VINCULATIONS)
            .select { filter { eq(COLUMN_CARETAKER_ID, caretakerId) } }
            .decodeSingleOrNull<Vinculation>() ?: return null
            
        return client.from(TABLE_PATIENTS)
            .select { filter { eq("id", vinculation.patientId) } }
            .decodeSingleOrNull()
    }

    /** Actualiza la geovalla del paciente. */
    suspend fun updateGeofence(patientUid: String, lat: Double, lng: Double, radius: Float) {
        client.from(TABLE_PATIENTS).update(
            {
                set("geofence_lat", lat)
                set("geofence_lng", lng)
                set("geofence_radius", radius)
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /** Actualiza la ubicación del paciente. */
    suspend fun updatePatientLocation(patientUid: String, lat: Double, lng: Double) {
        client.from(TABLE_PATIENTS).update(
            {
                set("patient_lat", lat)
                set("patient_lng", lng)
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /** Envía un "ping" al paciente cambiando un valor en la DB que su app observa. */
    suspend fun requestLocation(patientUid: String) {
        client.from(TABLE_PATIENTS).update(
            {
                set("location_request_trigger", UUID.randomUUID().toString())
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    private companion object {
        const val TABLE_PATIENTS = "patients"
        const val TABLE_CARETAKERS = "caretakers"
        const val TABLE_VINCULATIONS = "vinculations"
        const val COLUMN_UID = "uid"
        const val COLUMN_PATIENT_ID = "patient_id"
        const val COLUMN_CARETAKER_ID = "caretaker_id"
    }
}
