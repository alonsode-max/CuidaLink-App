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

    fun addEvent(event: Event) {
        _events.value = _events.value + event
    }

    fun removeEventForDate(event: Event, date: LocalDate) {
        _events.value = _events.value.mapNotNull { e ->
            if (e.id == event.id) {
                if (e.isRecurring) {
                    // Si es recurrente, no es trivial eliminar un solo día sin añadir excepciones.
                    // Por ahora, si es recurrente lo tratamos como eliminar todo o nada,
                    // o podrías implementar una lista de exclusión.
                    // Para simplificar según lo pedido, si es una fecha específica:
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
