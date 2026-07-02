package com.example.cuidalink.network

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.LocationHistoryInsert
import com.example.cuidalink.model.remote.LocationHistoryRow
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.model.remote.Vinculation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

    /**
     * Paciente asociado a la sesión: el propio si el usuario es paciente, o el
     * paciente vinculado si el usuario es cuidador. Null si no se puede resolver.
     */
    suspend fun fetchSessionPatient(): Patient? {
        fetchCurrentPatient()?.let { return it }
        val caretaker = fetchCurrentCaretaker() ?: return null
        return caretaker.id?.let { fetchLinkedPatient(it) }
    }

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

    /**
     * Observa en tiempo real (websockets) los cambios de la fila del paciente indicado.
     * Emite el [Patient] actualizado cada vez que el backend recibe un UPDATE. Se
     * suscribe al empezar a coleccionar y cancela el canal al terminar.
     */
    fun observePatientById(patientId: Long): Flow<Patient> {
        // Id único por suscripción: si dos pantallas observan al mismo paciente, no
        // pueden compartir un canal ya unido (supabase-kt lanza IllegalStateException).
        val channel = client.channel("patient_${patientId}_${UUID.randomUUID()}")
        return channel
            .postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = TABLE_PATIENTS
                filter("id", FilterOperator.EQ, patientId)
            }
            .mapNotNull { action -> runCatching { action.decodeRecord<Patient>() }.getOrNull() }
            .onStart { channel.subscribe() }
            .onCompletion { runCatching { channel.unsubscribe() } }
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

    /** Reemplaza la lista completa de geovallas del paciente (array JSON). */
    suspend fun setGeofences(patientUid: String, zones: List<com.example.cuidalink.model.remote.GeofenceZone>) {
        client.from(TABLE_PATIENTS).update({ set("geofences", zones) }) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /** Borra la geovalla del paciente (deja el paciente sin zona segura configurada). */
    suspend fun clearGeofence(patientUid: String) {
        client.from(TABLE_PATIENTS).update(
            buildJsonObject {
                put("geofence_lat", JsonNull)
                put("geofence_lng", JsonNull)
                put("geofence_radius", JsonNull)
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

    /**
     * Sube la foto de perfil del usuario autenticado al bucket [BUCKET_AVATARS] y guarda
     * su URL pública en la columna `profile_pic` de su tabla (paciente o cuidador).
     * Devuelve la URL guardada, o null si no hay sesión.
     *
     * El nombre del archivo es el uid (upsert), y añadimos `?v=<ms>` a la URL para que
     * Coil no reutilice la imagen cacheada tras reemplazarla.
     */
    suspend fun uploadProfilePhoto(bytes: ByteArray): String? {
        val uid = currentUid() ?: return null
        val path = "$uid.jpg"
        val bucket = client.storage.from(BUCKET_AVATARS)
        bucket.upload(path, bytes) { upsert = true }
        val url = bucket.publicUrl(path) + "?v=${System.currentTimeMillis()}"

        // La sesión es de un paciente o de un cuidador: actualiza la fila que exista.
        val table = if (fetchPatientByUid(uid) != null) TABLE_PATIENTS else TABLE_CARETAKERS
        client.from(table).update({ set("profile_pic", url) }) {
            filter { eq(COLUMN_UID, uid) }
        }
        return url
    }

    /** Inserta un punto en el historial de ubicaciones del paciente. */
    suspend fun insertLocationHistory(patientId: Long, lat: Double, lng: Double) {
        client.from(TABLE_LOCATION_HISTORY)
            .insert(LocationHistoryInsert(patientId = patientId, lat = lat, lng = lng))
    }

    /** Últimos puntos de ubicación del paciente (más recientes primero). */
    suspend fun fetchLocationHistory(patientId: Long, max: Long = 25): List<LocationHistoryRow> =
        client.from(TABLE_LOCATION_HISTORY)
            .select {
                filter { eq("patient_id", patientId) }
                order("created_at", Order.DESCENDING)
                limit(max)
            }
            .decodeList()

    /** Actualiza la telemetría del paciente (batería y pasos) para el cuidador. */
    suspend fun updatePatientMetrics(patientUid: String, batteryPercent: Int, steps: Int) {
        client.from(TABLE_PATIENTS).update(
            {
                set("battery_percent", batteryPercent)
                set("steps", steps)
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /**
     * Suma [minutesToAdd] a los minutos jugados y fija la última actividad.
     * Lee el total actual y lo reescribe (read-modify-write).
     */
    suspend fun addGameActivity(patientUid: String, minutesToAdd: Int, activity: String) {
        val current = fetchPatientByUid(patientUid)?.minutesPlayed ?: 0
        client.from(TABLE_PATIENTS).update(
            {
                set("minutes_played", current + minutesToAdd)
                set("last_activity", activity)
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /** Activa el SOS en la app del paciente cambiando un valor que su app observa. */
    suspend fun triggerSosAlert(patientUid: String) {
        client.from(TABLE_PATIENTS).update(
            {
                set("sos_alert_trigger", UUID.randomUUID().toString())
            }
        ) {
            filter { eq(COLUMN_UID, patientUid) }
        }
    }

    /**
     * El PACIENTE activa su SOS: escribe `patient_sos_trigger` en su propia fila
     * (por su uid de sesión), que el cuidador vinculado observa en tiempo real.
     */
    suspend fun triggerPatientSos() {
        val uid = currentUid() ?: return
        client.from(TABLE_PATIENTS).update(
            {
                set("patient_sos_trigger", UUID.randomUUID().toString())
            }
        ) {
            filter { eq(COLUMN_UID, uid) }
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
        const val TABLE_LOCATION_HISTORY = "location_history"
        const val BUCKET_AVATARS = "avatars"
        const val COLUMN_UID = "uid"
        const val COLUMN_PATIENT_ID = "patient_id"
        const val COLUMN_CARETAKER_ID = "caretaker_id"
    }
}
