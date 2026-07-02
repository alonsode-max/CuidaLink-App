package com.example.cuidalink.model

import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val time: LocalTime,
    val dates: List<LocalDate> = emptyList(), // Para eventos no recurrentes con fechas específicas
    val isRecurring: Boolean = false,
    val recurrenceInterval: Int? = null, // Cada cuántos días se repite
    val hasPeriod: Boolean = false,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val hasAlarm: Boolean = false
)
