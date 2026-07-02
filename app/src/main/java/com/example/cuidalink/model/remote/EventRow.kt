package com.example.cuidalink.model.remote

import com.example.cuidalink.model.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * DTO de la tabla `events` de Supabase. El calendario se comparte entre el paciente
 * y su cuidador: ambos leen/escriben las filas del mismo `patient_id`.
 *
 * Fechas y horas se guardan como texto ISO para que Postgrest las serialice sin líos:
 * `time` = "HH:mm", `dates`/`start_date`/`end_date` = "yyyy-MM-dd".
 */
@Serializable
data class EventRow(
    val id: String,
    @SerialName("patient_id") val patientId: Long,
    val name: String,
    val description: String? = null,
    val time: String,
    val dates: List<String> = emptyList(),
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("recurrence_interval") val recurrenceInterval: Int? = null,
    @SerialName("has_period") val hasPeriod: Boolean = false,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("has_alarm") val hasAlarm: Boolean = false
)

/** Convierte la fila del backend al modelo de dominio del calendario. */
fun EventRow.toDomain(): Event = Event(
    id = id,
    name = name,
    description = description,
    time = LocalTime.parse(time, TIME_FORMAT),
    dates = dates.map { LocalDate.parse(it) },
    isRecurring = isRecurring,
    recurrenceInterval = recurrenceInterval,
    hasPeriod = hasPeriod,
    startDate = startDate?.let { LocalDate.parse(it) },
    endDate = endDate?.let { LocalDate.parse(it) },
    hasAlarm = hasAlarm
)

/**
 * DTO de la tabla `event_completions`: registro de cada tarea/recordatorio marcado
 * como completado. Es la fuente del historial que ve el cuidador.
 */
@Serializable
data class EventCompletionRow(
    val id: String,
    @SerialName("patient_id") val patientId: Long,
    @SerialName("event_id") val eventId: String,
    @SerialName("event_name") val eventName: String,
    val date: String,
    @SerialName("completed_at") val completedAt: String? = null
)

/** Convierte un evento de dominio a fila del backend para el `patient_id` dado. */
fun Event.toRow(patientId: Long): EventRow = EventRow(
    id = id,
    patientId = patientId,
    name = name,
    description = description,
    time = time.format(TIME_FORMAT),
    dates = dates.map { it.toString() },
    isRecurring = isRecurring,
    recurrenceInterval = recurrenceInterval,
    hasPeriod = hasPeriod,
    startDate = startDate?.toString(),
    endDate = endDate?.toString(),
    hasAlarm = hasAlarm
)
