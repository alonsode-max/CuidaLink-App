package com.example.cuidalink.network

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.Patient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

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

    private companion object {
        const val TABLE_PATIENTS = "patients"
        const val TABLE_CARETAKERS = "caretakers"
        const val COLUMN_UID = "uid"
    }
}
