package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cuidalink.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class CalendarViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Tomas/recordatorios completados, indexados por evento y fecha.
    private val _completed = MutableStateFlow<Set<String>>(emptySet())
    val completed: StateFlow<Set<String>> = _completed.asStateFlow()

    fun completionKey(eventId: String, date: LocalDate): String = "$eventId|$date"

    fun isCompleted(eventId: String, date: LocalDate): Boolean =
        _completed.value.contains(completionKey(eventId, date))

    fun toggleCompleted(eventId: String, date: LocalDate) {
        val key = completionKey(eventId, date)
        _completed.value = if (_completed.value.contains(key)) {
            _completed.value - key
        } else {
            _completed.value + key
        }
    }

    fun addEvent(event: Event) {
        _events.value = _events.value + event
    }

    // Reemplaza un evento existente (mismo id) por su versión editada.
    fun updateEvent(updated: Event) {
        _events.value = _events.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeEventForDate(event: Event, date: LocalDate) {
        _events.value = _events.value.mapNotNull { e ->
            if (e.id == event.id) {
                if (e.isRecurring) {
                    // Si es recurrente, no es trivial eliminar un solo día sin añadir excepciones.
                    e.copy(dates = e.dates.filter { it != date })
                } else {
                    val newDates = e.dates.filter { it != date }
                    if (newDates.isEmpty()) null else e.copy(dates = newDates)
                }
            } else {
                e
            }
        }
    }

    fun removeAllEventsByName(name: String) {
        _events.value = _events.value.filter { it.name != name }
    }

    fun getEventsForDate(date: LocalDate): List<Event> {
        return _events.value.filter { event ->
            if (event.isRecurring) {
                event.recurringDays.contains(date.dayOfWeek.value)
            } else {
                event.dates.contains(date)
            }
        }
    }
}
