package com.example.cuidalink.repository

import com.example.cuidalink.model.Event
import com.example.cuidalink.model.remote.EventCompletionRow
import com.example.cuidalink.model.remote.EventRow
import com.example.cuidalink.model.remote.toDomain
import com.example.cuidalink.model.remote.toRow
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.util.UUID

/**
 * Acceso a la tabla `events` de Supabase. El calendario se comparte: tanto el
 * paciente como su cuidador operan sobre las filas del mismo `patient_id`.
 */
class CalendarRepository(
    private val client: SupabaseClient = SupabaseConfig.client,
    private val profileService: ProfileService = ProfileService()
) {

    /** Id del paciente cuyos eventos corresponden a la sesión (propio o vinculado). */
    suspend fun resolvePatientId(): Long? {
        // Espera a que Supabase restaure la sesión guardada; si no, tras reiniciar la
        // app currentUid() es null y no cargaríamos nada aunque haya sesión.
        runCatching { client.auth.awaitInitialization() }
        profileService.fetchCurrentPatient()?.id?.let { return it }
        val caretaker = profileService.fetchCurrentCaretaker() ?: return null
        return profileService.fetchLinkedPatient(caretaker.id!!)?.id
    }

    /** Todos los eventos del paciente. */
    suspend fun fetchEvents(patientId: Long): Result<List<Event>> = runCatching {
        client.from(TABLE_EVENTS)
            .select { filter { eq("patient_id", patientId) } }
            .decodeList<EventRow>()
            .map { it.toDomain() }
    }

    /** Inserta o actualiza un evento (upsert por `id`). */
    suspend fun upsertEvent(event: Event, patientId: Long): Result<Unit> = runCatching {
        client.from(TABLE_EVENTS).upsert(event.toRow(patientId))
        Unit
    }

    /** Borra un evento por su id. */
    suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        client.from(TABLE_EVENTS).delete { filter { eq("id", eventId) } }
    }

    /** Historial de tareas/recordatorios completados del paciente (más recientes primero). */
    suspend fun fetchCompletions(patientId: Long): Result<List<EventCompletionRow>> = runCatching {
        client.from(TABLE_COMPLETIONS)
            .select {
                filter { eq("patient_id", patientId) }
                order("completed_at", Order.DESCENDING)
            }
            .decodeList<EventCompletionRow>()
    }

    /** Marca una tarea como completada en una fecha (upsert por evento+fecha). */
    suspend fun addCompletion(
        patientId: Long,
        eventId: String,
        eventName: String,
        date: String
    ): Result<Unit> = runCatching {
        val row = EventCompletionRow(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            eventId = eventId,
            eventName = eventName,
            date = date
        )
        client.from(TABLE_COMPLETIONS).upsert(row) { onConflict = "event_id,date" }
    }

    /** Quita la marca de completado de una tarea en una fecha. */
    suspend fun removeCompletion(eventId: String, date: String): Result<Unit> = runCatching {
        client.from(TABLE_COMPLETIONS).delete {
            filter {
                eq("event_id", eventId)
                eq("date", date)
            }
        }
    }

    /** Emite (Unit) cada vez que cambian los eventos del paciente (realtime). */
    fun observeEvents(patientId: Long): Flow<Unit> = observeTable(TABLE_EVENTS, patientId)

    /** Emite (Unit) cada vez que cambian los completados del paciente (realtime). */
    fun observeCompletions(patientId: Long): Flow<Unit> =
        observeTable(TABLE_COMPLETIONS, patientId)

    private fun observeTable(table: String, patientId: Long): Flow<Unit> {
        val ch = client.channel("${table}_${patientId}_${UUID.randomUUID()}")
        return ch
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                this.table = table
                filter("patient_id", FilterOperator.EQ, patientId)
            }
            .map { }
            .onStart { ch.subscribe() }
            .onCompletion { runCatching { ch.unsubscribe() } }
    }

    private companion object {
        const val TABLE_EVENTS = "events"
        const val TABLE_COMPLETIONS = "event_completions"
    }
}
