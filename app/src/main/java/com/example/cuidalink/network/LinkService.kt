package com.example.cuidalink.network

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.remote.Vinculation
import com.example.cuidalink.model.remote.VinculationInsert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

/**
 * Llamadas Postgrest para la vinculación paciente ↔ cuidador.
 *
 * Cada función es una llamada de red suspendida; lanza si Supabase falla y
 * devuelve `null` cuando no existe la fila. El repositorio envuelve en Result.
 */
class LinkService(
    private val client: SupabaseClient = SupabaseConfig.client
) {

    /** UID del usuario autenticado en Supabase, o null si no hay sesión. */
    fun currentUid(): String? = client.auth.currentUserOrNull()?.id

    /** Paciente cuyo `code` coincide con el introducido por el cuidador. */
    suspend fun fetchPatientByCode(code: String): Patient? =
        client.from(TABLE_PATIENTS)
            .select { filter { eq(COLUMN_CODE, code) } }
            .decodeSingleOrNull()

    /** Paciente vinculado a un `uid` concreto. */
    suspend fun fetchPatientByUid(uid: String): Patient? =
        client.from(TABLE_PATIENTS)
            .select { filter { eq(COLUMN_UID, uid) } }
            .decodeSingleOrNull()

    /** Cuidador vinculado a un `uid` concreto. */
    suspend fun fetchCaretakerByUid(uid: String): Caretaker? =
        client.from(TABLE_CARETAKERS)
            .select { filter { eq(COLUMN_UID, uid) } }
            .decodeSingleOrNull()

    /** Guarda el código generado en la fila del paciente (UPDATE). */
    suspend fun assignPatientCode(uid: String, code: String) {
        client.from(TABLE_PATIENTS).update(
            { set(COLUMN_CODE, code) }
        ) {
            filter { eq(COLUMN_UID, uid) }
        }
    }

    /** Crea el registro de vinculación (POST a `vinculations`). */
    suspend fun insertVinculation(patientId: Long, caretakerId: Long) {
        client.from(TABLE_VINCULATIONS).insert(
            VinculationInsert(patientId = patientId, caretakerId = caretakerId)
        )
    }

    /** `true` si el paciente ya tiene al menos un cuidador vinculado. */
    suspend fun patientHasVinculation(patientId: Long): Boolean =
        client.from(TABLE_VINCULATIONS)
            .select { filter { eq(COLUMN_PATIENT_ID, patientId) } }
            .decodeList<Vinculation>()
            .isNotEmpty()

    /** `true` si el cuidador ya tiene al menos un paciente vinculado. */
    suspend fun caretakerHasVinculation(caretakerId: Long): Boolean =
        client.from(TABLE_VINCULATIONS)
            .select { filter { eq(COLUMN_CARETAKER_ID, caretakerId) } }
            .decodeList<Vinculation>()
            .isNotEmpty()

    private companion object {
        const val TABLE_PATIENTS = "patients"
        const val TABLE_CARETAKERS = "caretakers"
        const val TABLE_VINCULATIONS = "vinculations"
        const val COLUMN_UID = "uid"
        const val COLUMN_CODE = "code"
        const val COLUMN_PATIENT_ID = "patient_id"
        const val COLUMN_CARETAKER_ID = "caretaker_id"
    }
}
