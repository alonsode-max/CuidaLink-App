package com.example.cuidalink.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.theme.*
import com.example.cuidalink.util.scheduleAlarms
import com.example.cuidalink.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private val spanishLocale = Locale("es", "ES")

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddEventModal by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<Event?>(null) }

    val context = LocalContext.current
    val events by viewModel.events.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventModal = true },
                containerColor = CuidaGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir evento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 22.dp)
        ) {
            CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            WeekdayHeaderRow()

            Spacer(modifier = Modifier.height(6.dp))

            MonthGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                events = events,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CuidaDivider)
            Spacer(modifier = Modifier.height(12.dp))

            DayAgenda(
                selectedDate = selectedDate,
                events = viewModel.getEventsForDate(selectedDate),
                onEventClick = { selectedEventForDetails = it }
            )
        }

        if (showAddEventModal) {
            AddEventDialog(
                initialDate = selectedDate,
                onDismiss = { showAddEventModal = false },
                onSave = { event ->
                    viewModel.addEvent(event)
                    if (event.hasAlarm) {
                        scheduleAlarms(context, event)
                    }
                    showAddEventModal = false
                    Toast.makeText(context, "Evento guardado", Toast.LENGTH_SHORT).show()
                }
            )
        }

        selectedEventForDetails?.let { event ->
            EventDetailsDialog(
                event = event,
                date = selectedDate,
                onDismiss = { selectedEventForDetails = null },
                onDeleteDay = {
                    viewModel.removeEventForDate(event, selectedDate)
                    selectedEventForDetails = null
                    Toast.makeText(context, "Evento eliminado para este día", Toast.LENGTH_SHORT).show()
                },
                onDeleteAll = {
                    viewModel.removeAllEventsByName(event.name)
                    selectedEventForDetails = null
                    Toast.makeText(context, "Todas las alarmas de '${event.name}' eliminadas", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthLabel = "${currentMonth.month.getDisplayName(TextStyle.FULL, spanishLocale).replaceFirstChar { it.uppercase(spanishLocale) }} ${currentMonth.year}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Calendario",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonthNavButton(
                icon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = CuidaTextSecondary) },
                onClick = onPreviousMonth
            )
            Text(
                text = monthLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            MonthNavButton(
                icon = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = CuidaTextSecondary) },
                onClick = onNextMonth
            )
        }
    }
}

@Composable
private fun MonthNavButton(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, CuidaBorder, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextDisabled
            )
        }
    }
}

@Composable
private fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val offset = currentMonth.atDay(1).dayOfWeek.value - 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(offset) { Box(modifier = Modifier.height(48.dp)) }
        items(daysInMonth) { index ->
            val day = index + 1
            val date = currentMonth.atDay(day)
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            val dayEvents = events.filter { it.occursOn(date) }
            val hasEvents = dayEvents.isNotEmpty()

            val dayDescription = buildString {
                append(date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", spanishLocale)))
                if (isToday) append(", hoy")
                if (isSelected) append(", seleccionado")
                if (hasEvents) append(", con eventos")
            }

            Column(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { onDateSelected(date) }
                    .semantics { contentDescription = dayDescription },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) CuidaGreen else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = when {
                            isSelected -> Color.White
                            isToday -> CuidaGreen
                            else -> CuidaTextPrimary
                        }
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp).height(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    dayEvents.take(3).forEach { dayEvent ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(eventStyleFor(dayEvent.name).color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayAgenda(
    selectedDate: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val isToday = selectedDate == LocalDate.now()
    val dayLabel = selectedDate.format(DateTimeFormatter.ofPattern("EEEE d", spanishLocale))
    val title = if (isToday) "Hoy · $dayLabel" else dayLabel.replaceFirstChar { it.uppercase(spanishLocale) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (events.isEmpty()) {
            Text(
                text = "No hay eventos para este día",
                fontSize = 14.sp,
                color = CuidaTextSecondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(events.sortedBy { it.time }) { event ->
                    AgendaEventCard(event = event, onClick = { onEventClick(event) })
                }
            }
        }
    }
}

@Composable
private fun AgendaEventCard(event: Event, onClick: () -> Unit) {
    val style = eventStyleFor(event.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CuidaBorder, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "Evento ${event.name} a las ${event.time.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(style.container),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaTextPrimary
            )
            if (!event.description.isNullOrBlank()) {
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    color = CuidaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = style.color
        )
    }
}

private fun Event.occursOn(date: LocalDate): Boolean {
    return if (isRecurring) recurringDays.contains(date.dayOfWeek.value) else dates.contains(date)
}

// Icono y color según el tipo de actividad detectado en el nombre del evento,
// imitando las categorías de color del diseño (verde, ámbar, azul).
private data class EventStyle(
    val icon: ImageVector,
    val color: Color,
    val container: Color
)

private fun eventStyleFor(name: String): EventStyle {
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
            EventStyle(Icons.Filled.Restaurant, CuidaRed, Color(0xFFFCEAE8))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(initialDate: LocalDate, onDismiss: () -> Unit, onSave: (Event) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedDates by remember { mutableStateOf(setOf(initialDate)) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var hasAlarm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Nuevo evento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del evento") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRecurring) {
                    Text("Selecciona días:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            DatePickerDialog(context, { _, year, month, day ->
                                val date = LocalDate.of(year, month + 1, day)
                                selectedDates = selectedDates + date
                            }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CuidaGreenSurface, contentColor = CuidaGreenDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Añadir día", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        selectedDates.toList().sorted().forEach { date ->
                            AssistChip(
                                onClick = { if (selectedDates.size > 1) selectedDates = selectedDates - date },
                                label = { Text(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Quitar fecha ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Hora: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button) {
                            TimePickerDialog(context, { _, hour, minute ->
                                selectedTime = LocalTime.of(hour, minute)
                            }, selectedTime.hour, selectedTime.minute, true).show()
                        }
                        .semantics { contentDescription = "Cambiar hora, actual ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}" }
                        .padding(4.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CuidaGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Es recurrente?", color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Repetir los días:", color = CuidaTextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val days = listOf("L", "M", "X", "J", "V", "S", "D")
                        days.forEachIndexed { index, day ->
                            val dayNum = index + 1
                            val isSelected = selectedDays.contains(dayNum)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) selectedDays - dayNum else selectedDays + dayNum
                                },
                                label = { Text(day) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar alarma", color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it })
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            onSave(Event(
                                name = name,
                                description = if (description.isBlank()) null else description,
                                time = selectedTime,
                                dates = if (isRecurring) emptyList() else selectedDates.toList(),
                                isRecurring = isRecurring,
                                recurringDays = selectedDays.toList(),
                                hasAlarm = hasAlarm
                            ))
                        },
                        enabled = name.isNotBlank() && (isRecurring && selectedDays.isNotEmpty() || !isRecurring),
                        colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailsDialog(
    event: Event,
    date: LocalDate,
    onDismiss: () -> Unit,
    onDeleteDay: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val style = eventStyleFor(event.name)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(style.container),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        event.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CuidaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = CuidaTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${event.time.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", spanishLocale))}",
                        fontSize = 15.sp,
                        color = CuidaTextPrimary
                    )
                }

                if (!event.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(20.dp), tint = CuidaTextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.description, fontSize = 14.sp, color = CuidaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = CuidaDivider)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Eliminar:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CuidaRed)

                TextButton(
                    onClick = onDeleteDay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = CuidaRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solo este día")
                }

                TextButton(
                    onClick = onDeleteAll,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = CuidaRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Todas las alarmas con este nombre")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Cerrar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
