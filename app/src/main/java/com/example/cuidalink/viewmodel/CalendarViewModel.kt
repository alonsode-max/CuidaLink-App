package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cuidalink.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
                    // Para eventos recurrentes, eliminar un día específico requeriría una lista de exclusión.
                    // Por ahora, mantenemos la lógica simple de eliminar el evento completo o ignorar la petición.
                    e
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
                val start = event.startDate ?: return@filter false
                
                // Si la fecha es anterior al inicio, no se muestra
                if (date.isBefore(start)) return@filter false
                
                // Si tiene fin y la fecha es posterior, no se muestra
                if (event.hasPeriod && event.endDate != null && date.isAfter(event.endDate)) {
                    return@filter false
                }
                
                // Comprobamos si coincide con el intervalo de días
                val daysBetween = ChronoUnit.DAYS.between(start, date)
                val interval = event.recurrenceInterval ?: 1
                daysBetween % interval == 0L
            } else {
                event.dates.contains(date)
            }
        }
    }
}
