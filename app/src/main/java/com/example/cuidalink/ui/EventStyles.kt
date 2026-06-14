package com.example.cuidalink.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaAmber
import com.example.cuidalink.ui.theme.CuidaAmberSurface
import com.example.cuidalink.ui.theme.CuidaBlue
import com.example.cuidalink.ui.theme.CuidaBlueSurface
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
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
    "medic", "pastilla", "insulina", "jarabe", "dosis", "tomar", "vitamina"
)

internal fun isMedicationEvent(name: String): Boolean {
    val normalized = name.lowercase(spanishLocale)
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

    return when {
        isMedicationEvent(name) ->
            EventStyle(HugeIcons.Medicine, CuidaAmber, CuidaAmberSurface)
        matches("paseo", "pasear", "caminar", "andar", "parque") ->
            EventStyle(HugeIcons.Walking, CuidaBlue, CuidaBlueSurface)
        matches("cita", "médico", "medico", "doctor", "hospital", "consulta", "revisión", "revision") ->
            EventStyle(HugeIcons.Stethoscope, CuidaGreen, CuidaGreenSurface)
        matches("comida", "comer", "desayuno", "cena", "almuerzo", "merienda") ->
            EventStyle(HugeIcons.Restaurant, CuidaRed, CuidaRedSurface)
        matches("ejercicio", "gimnasia", "deporte", "yoga", "estiramiento") ->
            EventStyle(HugeIcons.Dumbbell, CuidaBlue, CuidaBlueSurface)
        matches("llamar", "llamada", "teléfono", "telefono", "videollamada") ->
            EventStyle(HugeIcons.Call, CuidaGreen, CuidaGreenSurface)
        matches("juego", "jugar", "memoria") ->
            EventStyle(HugeIcons.Puzzle, CuidaAmber, CuidaAmberSurface)
        else -> {
            // Sin categoría conocida: color estable derivado del nombre,
            // para que eventos distintos no compartan todos el mismo color.
            val fallbackPalette = listOf(
                EventStyle(HugeIcons.CalendarCheck, CuidaGreen, CuidaGreenSurface),
                EventStyle(HugeIcons.CalendarCheck, CuidaAmber, CuidaAmberSurface),
                EventStyle(HugeIcons.CalendarCheck, CuidaBlue, CuidaBlueSurface)
            )
            fallbackPalette[abs(name.hashCode()) % fallbackPalette.size]
        }
    }
}
