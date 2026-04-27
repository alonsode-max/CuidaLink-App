package com.example.cuidalink.model

import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val time: LocalTime,
    val dates: List<LocalDate> = emptyList(), // Soporta múltiples fechas
    val isRecurring: Boolean = false,
    val recurringDays: List<Int> = emptyList(), // 1 (Lunes) a 7 (Domingo)
    val hasAlarm: Boolean = false
)
