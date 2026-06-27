package com.example.cuidalink.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaHeaderTeal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

internal val spanishLocale = Locale("es", "ES")

internal fun Event.occursOn(date: LocalDate): Boolean {
    return if (isRecurring) {
        val start = startDate ?: return false
        if (date.isBefore(start)) return false
        if (hasPeriod && endDate != null && date.isAfter(endDate)) return false
        
        val interval = recurrenceInterval ?: 1
        val daysBetween = ChronoUnit.DAYS.between(start, date)
        daysBetween % interval == 0L
    } else {
        dates.contains(date)
    }
}

// Palabras clave que identifican un evento de medicacion.
private val medicationKeywords = listOf(
    "medicament", "medicina", "medicación", "medicacion", "pastilla",
    "insulina", "jarabe", "dosis", "tomar", "vitamina", "comprimido"
)

// Palabras de cita/visita médica: NO son medicación aunque contengan "medic"
private val appointmentKeywords = listOf(
    "médico", "medico", "doctor", "doctora", "hospital", "cita",
    "consulta", "revisión", "revision", "visita"
)

internal fun isMedicationEvent(name: String): Boolean {
    val normalized = name.lowercase(spanishLocale)
    // Una visita al médico no es una toma de medicación.
    if (appointmentKeywords.any { normalized.contains(it) }) return false
    return medicationKeywords.any { normalized.contains(it) }
}

// Icono y color según el tipo de actividad detectado en el nombre del evento,
internal data class EventStyle(
    val icon: ImageVector,
    val color: Color,
    val container: Color
)

internal fun eventStyleFor(name: String): EventStyle {
    val normalized = name.lowercase(spanishLocale)
    fun matches(vararg keywords: String) = keywords.any { normalized.contains(it) }

    // Paleta en familia verde: todas las cajas combinan entre sí. Se distingue
    return when {
        isMedicationEvent(name) ->
            EventStyle(HugeIcons.Medicine, CuidaGreen, CuidaGreenSurface)
        matches("paseo", "pasear", "caminar", "andar", "parque") ->
            EventStyle(HugeIcons.Walking, CuidaHeaderTeal, CuidaGreenSurfaceHover)
        matches("cita", "médico", "medico", "doctor", "hospital", "consulta", "revisión", "revision", "visita") ->
            EventStyle(HugeIcons.Stethoscope, CuidaGreenDark, CuidaGreenSurface)
        matches("comida", "comer", "desayuno", "cena", "almuerzo", "merienda") ->
            EventStyle(HugeIcons.Restaurant, CuidaGreen, CuidaGreenSurfaceHover)
        matches("ejercicio", "gimnasia", "deporte", "yoga", "estiramiento") ->
            EventStyle(HugeIcons.Dumbbell, CuidaHeaderTeal, CuidaGreenSurface)
        matches("llamar", "llamada", "teléfono", "telefono", "videollamada") ->
            EventStyle(HugeIcons.Call, CuidaGreen, CuidaGreenSurface)
        matches("juego", "jugar", "memoria") ->
            EventStyle(HugeIcons.Puzzle, CuidaGreenDark, CuidaGreenSurfaceHover)
        else -> {
            // Sin categoría conocida: tono estable derivado del nombre, siempre
            val fallbackPalette = listOf(
                EventStyle(HugeIcons.CalendarCheck, CuidaGreen, CuidaGreenSurface),
                EventStyle(HugeIcons.CalendarCheck, CuidaGreenDark, CuidaGreenSurfaceHover),
                EventStyle(HugeIcons.CalendarCheck, CuidaHeaderTeal, CuidaGreenSurface)
            )
            fallbackPalette[abs(name.hashCode()) % fallbackPalette.size]
        }
    }
}
