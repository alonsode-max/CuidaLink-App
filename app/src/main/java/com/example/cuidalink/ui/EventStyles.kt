package com.example.cuidalink.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cuidalink.model.Event
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
        matches("medic", "pastilla", "insulina", "jarabe", "dosis", "tomar", "vitamina") ->
            EventStyle(Icons.Filled.Medication, CuidaAmber, CuidaAmberSurface)
        matches("paseo", "pasear", "caminar", "andar", "parque") ->
            EventStyle(Icons.AutoMirrored.Filled.DirectionsWalk, CuidaBlue, CuidaBlueSurface)
        matches("cita", "médico", "medico", "doctor", "hospital", "consulta", "revisión", "revision") ->
            EventStyle(Icons.Filled.MedicalServices, CuidaGreen, CuidaGreenSurface)
        matches("comida", "comer", "desayuno", "cena", "almuerzo", "merienda") ->
            EventStyle(Icons.Filled.Restaurant, CuidaRed, CuidaRedSurface)
        matches("ejercicio", "gimnasia", "deporte", "yoga", "estiramiento") ->
            EventStyle(Icons.Filled.FitnessCenter, CuidaBlue, CuidaBlueSurface)
        matches("llamar", "llamada", "teléfono", "telefono", "videollamada") ->
            EventStyle(Icons.Filled.Call, CuidaGreen, CuidaGreenSurface)
        matches("juego", "jugar", "memoria") ->
            EventStyle(Icons.Filled.Extension, CuidaAmber, CuidaAmberSurface)
        else -> {
            // Sin categoría conocida: color estable derivado del nombre,
            // para que eventos distintos no compartan todos el mismo color.
            val fallbackPalette = listOf(
                EventStyle(Icons.Filled.Event, CuidaGreen, CuidaGreenSurface),
                EventStyle(Icons.Filled.Event, CuidaAmber, CuidaAmberSurface),
                EventStyle(Icons.Filled.Event, CuidaBlue, CuidaBlueSurface)
            )
            fallbackPalette[abs(name.hashCode()) % fallbackPalette.size]
        }
    }
}
