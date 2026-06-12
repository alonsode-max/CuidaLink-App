package com.example.cuidalink.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.theme.CuidaBorder
import com.example.cuidalink.ui.theme.CuidaDivider
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaSurfaceFaint
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextDisabled
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Datos del paciente de ejemplo (diseño 1A). Sustituir cuando exista un
// ViewModel de perfil con datos reales del backend.
private const val PATIENT_NAME = "Carmen"
private const val PATIENT_INITIALS = "CD"
private const val EMERGENCY_PHONE = "112"

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = viewModel(),
    onOpenCalendar: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onPlay: () -> Unit = {}
) {
    val events by calendarViewModel.events.collectAsState()
    val today = LocalDate.now()
    val todayEvents = events
        .filter { it.occursOn(today) }
        .sortedBy { it.time }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        DashboardHeader(onOpenCalendar = onOpenCalendar, onOpenProfile = onOpenProfile)
        GreetingBlock()
        SummaryCardsRow(todayEvents = todayEvents, onOpenCalendar = onOpenCalendar)
        QuickActions(onPlay = onPlay)
        TodayTasks(todayEvents = todayEvents)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DashboardHeader(onOpenCalendar: () -> Unit, onOpenProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(CuidaGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "CuidaLink",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onOpenCalendar,
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, CuidaBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Abrir calendario",
                tint = CuidaTextSecondary
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CuidaGreenSurface)
                .clickable(role = Role.Button, onClick = onOpenProfile)
                .semantics { contentDescription = "Abrir mi perfil" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = PATIENT_INITIALS,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
    }
}

@Composable
private fun GreetingBlock() {
    val now = LocalTime.now()
    val greeting = when {
        now.hour < 12 -> "Buenos días,"
        now.hour < 20 -> "Buenas tardes,"
        else -> "Buenas noches,"
    }
    val dateLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", spanishLocale))
        .replaceFirstChar { it.uppercase(spanishLocale) }

    Column {
        Text(text = greeting, fontSize = 16.sp, color = CuidaTextSecondary)
        Text(
            text = PATIENT_NAME,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(
            text = dateLabel,
            fontSize = 14.sp,
            color = CuidaTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SummaryCardsRow(
    todayEvents: List<Event>,
    onOpenCalendar: () -> Unit
) {
    val nextEvent = todayEvents.firstOrNull { it.time >= LocalTime.now() }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tarjeta de clima estática como en el diseño: la app no consume
        // ninguna API meteorológica.
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(CuidaSurfaceMuted)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFE0A93E),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "21°",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
            }
            Text(text = "Soleado · Madrid", fontSize = 13.sp, color = CuidaTextSecondary)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(CuidaGreenSurface)
                .clickable(role = Role.Button, onClick = onOpenCalendar)
                .semantics {
                    contentDescription = if (nextEvent != null) {
                        "Próxima actividad: ${nextEvent.name} a las " +
                            nextEvent.time.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            ". Abrir calendario"
                    } else {
                        "Sin más actividades hoy. Abrir calendario"
                    }
                }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = nextEvent?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
            Text(
                text = nextEvent?.let { "Próxima · ${it.name}" } ?: "Sin más actividades hoy",
                fontSize = 13.sp,
                color = CuidaGreenDark
            )
        }
    }
}

@Composable
private fun QuickActions(onPlay: () -> Unit) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Acciones rápidas",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CuidaRed)
                    .clickable(role = Role.Button) {
                        // Abre el marcador con el 112 ya escrito; la llamada
                        // la confirma la persona usuaria (más seguro que llamar solo).
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$EMERGENCY_PHONE"))
                        context.startActivity(intent)
                    }
                    .semantics { contentDescription = "SOS, pedir ayuda. Llama al $EMERGENCY_PHONE" }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "SOS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Pedir ayuda",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CuidaGreenSurface)
                    .clickable(role = Role.Button, onClick = onPlay)
                    .semantics { contentDescription = "Jugar al juego de memoria" }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = CuidaGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Jugar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CuidaGreenDark
                    )
                    Text(
                        text = "Juego de memoria",
                        fontSize = 13.sp,
                        color = CuidaGreenDark.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayTasks(todayEvents: List<Event>) {
    val now = LocalTime.now()
    val doneCount = todayEvents.count { it.time < now }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tareas de hoy",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (todayEvents.isNotEmpty()) {
                Text(
                    text = "$doneCount de ${todayEvents.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CuidaGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CuidaGreenSurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        if (todayEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CuidaSurfaceFaint)
                    .border(1.dp, CuidaDivider, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay actividades para hoy.\nAñádelas desde el calendario.",
                    fontSize = 14.sp,
                    color = CuidaTextSecondary
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todayEvents.forEach { event ->
                    TodayTaskRow(
                        name = event.name,
                        time = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        // Sin registro de tareas completadas en el modelo:
                        // se marca como hecha cuando su hora ya pasó.
                        isDone = event.time < now
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayTaskRow(name: String, time: String, isDone: Boolean) {
    val stateLabel = if (isDone) "hecha" else "pendiente"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDone) CuidaSurfaceFaint else Color.White)
            .border(1.dp, if (isDone) CuidaDivider else CuidaBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .semantics { contentDescription = "Tarea $stateLabel: $name a las $time" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isDone) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CuidaGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(2.dp, Color(0xFFCBD7D1), CircleShape)
            )
        }
        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDone) CuidaTextDisabled else CuidaTextPrimary,
            textDecoration = if (isDone) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = time,
            fontSize = 13.sp,
            color = if (isDone) CuidaTextDisabled else CuidaTextSecondary
        )
    }
}
