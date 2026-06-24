package com.example.cuidalink.ui

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.Event
import com.example.cuidalink.receiver.AlarmReceiver
import com.example.cuidalink.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

enum class CalendarView {
    DAY, WEEK, MONTH
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    var currentView by remember { mutableStateOf(CalendarView.MONTH) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddEventModal by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<Event?>(null) }
    
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEventModal = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Evento")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SegmentedButton(
                    selected = currentView == CalendarView.DAY,
                    onClick = { currentView = CalendarView.DAY },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) { Text("Día") }
                SegmentedButton(
                    selected = currentView == CalendarView.WEEK,
                    onClick = { currentView = CalendarView.WEEK },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) { Text("Semana") }
                SegmentedButton(
                    selected = currentView == CalendarView.MONTH,
                    onClick = { currentView = CalendarView.MONTH },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) { Text("Mes") }
            }

            when (currentView) {
                CalendarView.DAY -> DayView(selectedDate, viewModel.getEventsForDate(selectedDate)) { selectedEventForDetails = it }
                CalendarView.WEEK -> WeekView(selectedDate, viewModel) { selectedEventForDetails = it }
                CalendarView.MONTH -> MonthView(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewModel = viewModel,
                    onEventClick = { selectedEventForDetails = it }
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(initialDate: LocalDate, onDismiss: () -> Unit, onSave: (Event) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceInterval by remember { mutableStateOf("1") }
    var hasPeriod by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(initialDate) }
    var endDate by remember { mutableStateOf(initialDate.plusMonths(1)) }
    
    var selectedDates by remember { mutableStateOf(setOf(initialDate)) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var hasAlarm by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Nuevo Evento", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del evento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Hora: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}", 
                    modifier = Modifier.clickable {
                        TimePickerDialog(context, { _, hour, minute ->
                            selectedTime = LocalTime.of(hour, minute)
                        }, selectedTime.hour, selectedTime.minute, true).show()
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Es recurrente?")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recurrenceInterval,
                        onValueChange = { if (it.all { char -> char.isDigit() }) recurrenceInterval = it },
                        label = { Text("Repetir cada (días)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¿Tiene un plazo?")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(checked = hasPeriod, onCheckedChange = { hasPeriod = it })
                    }
                    
                    if (hasPeriod) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha de inicio: ${startDate.format(dateFormatter)}",
                            modifier = Modifier.clickable {
                                DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, 
                                    startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha de fin: ${endDate.format(dateFormatter)}",
                            modifier = Modifier.clickable {
                                DatePickerDialog(context, { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) }, 
                                    endDate.year, endDate.monthValue - 1, endDate.dayOfMonth).show()
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha de inicio: ${startDate.format(dateFormatter)}",
                            modifier = Modifier.clickable {
                                DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, 
                                    startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selecciona días:", style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            DatePickerDialog(context, { _, year, month, day ->
                                selectedDates = selectedDates + LocalDate.of(year, month + 1, day)
                            }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show()
                        }) { Text("Añadir día") }
                    }
                    selectedDates.toList().sorted().forEach { date ->
                        AssistChip(
                            onClick = { if (selectedDates.size > 1) selectedDates = selectedDates - date },
                            label = { Text(date.format(dateFormatter)) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar Alarma")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it })
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        onSave(Event(
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            time = selectedTime,
                            dates = if (isRecurring) emptyList() else selectedDates.toList(),
                            isRecurring = isRecurring,
                            recurrenceInterval = recurrenceInterval.toIntOrNull() ?: 1,
                            hasPeriod = hasPeriod,
                            startDate = if (isRecurring) startDate else null,
                            endDate = if (isRecurring && hasPeriod) endDate else null,
                            hasAlarm = hasAlarm
                        ))
                    }, enabled = name.isNotBlank()) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
fun EventDetailsDialog(event: Event, date: LocalDate, onDismiss: () -> Unit, onDeleteDay: () -> Unit, onDeleteAll: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.name, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(20.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${event.time.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("es", "ES")))}")
                }
                if (!event.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Notes, null, Modifier.size(20.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Eliminar:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onDeleteDay, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solo este día")
                }
                TextButton(onClick = onDeleteAll, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Todas las alarmas con este nombre")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
fun DayView(date: LocalDate, events: List<Event>, onEventClick: (Event) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        if (events.isEmpty()) {
            Text("No hay eventos para este día", color = Color.Gray)
        } else {
            LazyColumn { items(events) { EventItem(it, onClick = { onEventClick(it) }) } }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(event.time.format(DateTimeFormatter.ofPattern("HH:mm")), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(event.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (!event.description.isNullOrBlank()) Text(event.description, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun WeekView(selectedDate: LocalDate, viewModel: CalendarViewModel, onEventClick: (Event) -> Unit) {
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Semana del ${startOfWeek.dayOfMonth} al ${startOfWeek.plusDays(6).dayOfMonth} de ${startOfWeek.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn {
            items(weekDays) { day ->
                val dayEvents = viewModel.getEventsForDate(day)
                Column {
                    Text("${day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))} ${day.dayOfMonth}", style = MaterialTheme.typography.labelLarge, color = if (day == LocalDate.now()) MaterialTheme.colorScheme.primary else Color.Gray)
                    if (dayEvents.isEmpty()) Text("Sin eventos", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.bodySmall)
                    else dayEvents.forEach { EventItem(it, onClick = { onEventClick(it) }) }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MonthView(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit, viewModel: CalendarViewModel, onEventClick: (Event) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val offset = currentMonth.atDay(1).dayOfWeek.value - 1

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, null) }
            Text("${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, null) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(offset) { Box(Modifier.aspectRatio(1f)) }
            items(daysInMonth) { index ->
                val date = currentMonth.atDay(index + 1)
                val isSelected = date == selectedDate
                val isToday = date == LocalDate.now()
                val hasEvents = viewModel.getEventsForDate(date).isNotEmpty()

                Box(modifier = Modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent).clickable { onDateSelected(date) }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.dayOfMonth.toString(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        if (hasEvents) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Eventos del día:", style = MaterialTheme.typography.titleSmall)
        val selectedDayEvents = viewModel.getEventsForDate(selectedDate)
        if (selectedDayEvents.isEmpty()) Text("No hay eventos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        else LazyColumn { items(selectedDayEvents) { EventItem(it, onClick = { onEventClick(it) }) } }
    }
}

fun scheduleAlarms(context: Context, event: Event) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("EVENT_NAME", event.name) }

    if (event.isRecurring) {
        val intervalMillis = (event.recurrenceInterval ?: 1).toLong() * 24 * 60 * 60 * 1000
        val calendar = Calendar.getInstance().apply {
            val start = event.startDate ?: LocalDate.now()
            set(start.year, start.monthValue - 1, start.dayOfMonth, event.time.hour, event.time.minute, 0)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, event.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intervalMillis, pendingIntent)
    } else {
        event.dates.forEach { date ->
            val calendar = Calendar.getInstance().apply { set(date.year, date.monthValue - 1, date.dayOfMonth, event.time.hour, event.time.minute, 0) }
            if (calendar.after(Calendar.getInstance())) {
                val pendingIntent = PendingIntent.getBroadcast(context, (event.id + date).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }
}
