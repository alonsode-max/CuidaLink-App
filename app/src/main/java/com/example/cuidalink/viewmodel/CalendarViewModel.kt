package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.Event
import com.example.cuidalink.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Calendario compartido entre el paciente y su cuidador: los eventos viven en la
 * tabla `events` de Supabase (por `patient_id`), así ambos ven lo mismo.
 *
 * La lista local actúa como caché optimista: se actualiza al instante y se persiste
 * en segundo plano para que la UI siga siendo fluida.
 */
class CalendarViewModel(
    private val repository: CalendarRepository = CalendarRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Tomas/recordatorios completados, indexados por evento y fecha (estado local).
    private val _completed = MutableStateFlow<Set<String>>(emptySet())
    val completed: StateFlow<Set<String>> = _completed.asStateFlow()

    // Paciente cuyos eventos gestionamos (propio si es paciente, vinculado si es cuidador).
    private var patientId: Long? = null
    private var realtimeStarted = false

    init {
        loadEvents()
    }

    /** Resuelve el paciente y carga sus eventos + completados desde Supabase. */
    fun loadEvents() {
        viewModelScope.launch {
            val id = patientId ?: repository.resolvePatientId() ?: return@launch
            patientId = id
            repository.fetchEvents(id).onSuccess { _events.value = it }
            repository.fetchCompletions(id).onSuccess { rows ->
                _completed.value = rows
                    .map { completionKey(it.eventId, java.time.LocalDate.parse(it.date)) }
                    .toSet()
            }
            startRealtime(id)
        }
    }

    // Sincroniza en vivo entre paciente y cuidador: al cambiar eventos/completados
    // en el backend, recargamos. Se arranca una sola vez.
    private fun startRealtime(id: Long) {
        if (realtimeStarted) return
        realtimeStarted = true
        viewModelScope.launch {
            repository.observeEvents(id).collect {
                repository.fetchEvents(id).onSuccess { _events.value = it }
            }
        }
        viewModelScope.launch {
            repository.observeCompletions(id).collect {
                repository.fetchCompletions(id).onSuccess { rows ->
                    _completed.value = rows
                        .map { completionKey(it.eventId, java.time.LocalDate.parse(it.date)) }
                        .toSet()
                }
            }
        }
    }

    fun completionKey(eventId: String, date: LocalDate): String = "$eventId|$date"

    fun isCompleted(eventId: String, date: LocalDate): Boolean =
        _completed.value.contains(completionKey(eventId, date))

    fun toggleCompleted(eventId: String, date: LocalDate) {
        val key = completionKey(eventId, date)
        val wasCompleted = _completed.value.contains(key)
        _completed.value = if (wasCompleted) _completed.value - key else _completed.value + key

        // Persistir en Supabase para que quede en el historial compartido.
        val name = _events.value.firstOrNull { it.id == eventId }?.name ?: "Tarea"
        val isoDate = date.toString()
        viewModelScope.launch {
            val id = patientId ?: repository.resolvePatientId()?.also { patientId = it } ?: return@launch
            if (wasCompleted) {
                repository.removeCompletion(eventId, isoDate)
            } else {
                repository.addCompletion(id, eventId, name, isoDate)
            }
        }
    }

    fun addEvent(event: Event) {
        _events.value = _events.value + event
        persist(event)
    }

    // Reemplaza un evento existente (mismo id) por su versión editada.
    fun updateEvent(updated: Event) {
        _events.value = _events.value.map { if (it.id == updated.id) updated else it }
        persist(updated)
    }

    fun removeEventForDate(event: Event, date: LocalDate) {
        var toDeleteId: String? = null
        var toUpsert: Event? = null

        _events.value = _events.value.mapNotNull { e ->
            if (e.id == event.id) {
                if (e.isRecurring) {
                    e.copy(dates = e.dates.filter { it != date }).also { toUpsert = it }
                } else {
                    val newDates = e.dates.filter { it != date }
                    if (newDates.isEmpty()) {
                        toDeleteId = e.id
                        null
                    } else {
                        e.copy(dates = newDates).also { toUpsert = it }
                    }
                }
            } else {
                e
            }
        }

        toDeleteId?.let { id -> viewModelScope.launch { repository.deleteEvent(id) } }
        toUpsert?.let { persist(it) }
    }

    fun removeAllEventsByName(name: String) {
        val toRemove = _events.value.filter { it.name == name }
        _events.value = _events.value.filter { it.name != name }
        viewModelScope.launch {
            toRemove.forEach { repository.deleteEvent(it.id) }
        }
    }

    fun getEventsForDate(date: LocalDate): List<Event> {
        return _events.value.filter { event ->
            if (event.isRecurring) {
                val start = event.startDate ?: return@filter false
                if (date.isBefore(start)) return@filter false
                if (event.hasPeriod && event.endDate != null && date.isAfter(event.endDate)) {
                    return@filter false
                }
                val daysBetween = ChronoUnit.DAYS.between(start, date)
                val interval = event.recurrenceInterval ?: 1
                daysBetween % interval == 0L
            } else {
                event.dates.contains(date)
            }
        }
    }

    /** Guarda el evento en Supabase (resuelve el paciente si aún no se conocía). */
    private fun persist(event: Event) {
        viewModelScope.launch {
            val id = patientId ?: repository.resolvePatientId()?.also { patientId = it } ?: return@launch
            repository.upsertEvent(event, id)
        }
    }
}
