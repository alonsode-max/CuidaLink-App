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
import java.util.Locale
import kotlin.math.abs

internal val spanishLocale = Locale("es", "ES")

internal fun Event.occursOn(date: LocalDate): Boolean {
    return if (isRecurring) recurringDays.contains(date.dayOfWeek.value) else dates.contains(date)
}

// Palabras clave que identifican un evento de medicación. Se comparten con
// eventStyleFor para que la tarjeta de "Próxima medicación" del Home use el
// mismo criterio que el icono de la línea de tiempo.
private val medicationKeywords = listOf(
    "medicament", "medicina", "medicación", "medicacion", "pastilla",
    "insulina", "jarabe", "dosis", "tomar", "vitamina", "comprimido"
)

// Palabras de cita/visita médica: NO son medicación aunque contengan "medic"
// (p. ej. "ir al medico"). Se excluyen para que no sumen en el widget de
// medicación del Home.
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
// imitando las categorías de color del diseño (verde, ámbar, azul).
internal data class EventStyle(
    val icon: ImageVector,
    val color: Color,
    val container: Color
)

internal fun eventStyleFor(name: String): EventStyle {
    val normalized = name.lowercase(spanishLocale)
    fun matches(vararg keywords: String) = keywords.any { normalized.contains(it) }

    // Paleta en familia verde: todas las cajas combinan entre sí. Se distingue
    // la categoría por el icono y por el tono de acento (verde / verde oscuro /
    // teal), no por colores que choquen (ámbar, rojo, azul).
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
            // dentro de la familia verde.
            val fallbackPalette = listOf(
                EventStyle(HugeIcons.CalendarCheck, CuidaGreen, CuidaGreenSurface),
                EventStyle(HugeIcons.CalendarCheck, CuidaGreenDark, CuidaGreenSurfaceHover),
                EventStyle(HugeIcons.CalendarCheck, CuidaHeaderTeal, CuidaGreenSurface)
            )
            fallbackPalette[abs(name.hashCode()) % fallbackPalette.size]
        }
    }
}
