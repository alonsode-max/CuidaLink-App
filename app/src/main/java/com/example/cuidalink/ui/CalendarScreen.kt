package com.example.cuidalink.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.*
import com.example.cuidalink.util.scheduleAlarms
import com.example.cuidalink.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddEventModal by remember { mutableStateOf(false) }
    var selectedEventForDetails by remember { mutableStateOf<Event?>(null) }

    val context = LocalContext.current
    val events by viewModel.events.collectAsState()
    val completedKeys by viewModel.completed.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Solo el inset inferior; el superior lo aporta el NavHost.
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Header compacto: indica al paciente que está en el Calendario.
            ScreenHeader(title = "Calendario", icon = HugeIcons.Calendar)

            // Panel de contenido: esquinas superiores redondeadas que se solapan
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp)
            ) {
                CalendarCard(
                    selectedDate = selectedDate,
                    events = events,
                    onPreviousWeek = { selectedDate = selectedDate.minusWeeks(1) },
                    onNextWeek = { selectedDate = selectedDate.plusWeeks(1) },
                    onDateSelected = { selectedDate = it },
                    onAddReminder = { showAddEventModal = true }
                )

                Spacer(modifier = Modifier.height(20.dp))

                DayAgenda(
                    selectedDate = selectedDate,
                    events = viewModel.getEventsForDate(selectedDate),
                    completedKeys = completedKeys,
                    onEventClick = { selectedEventForDetails = it },
                    onToggleCompleted = { viewModel.toggleCompleted(it.id, selectedDate) },
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
                onSave = { updated ->
                    viewModel.updateEvent(updated)
                    if (updated.hasAlarm) {
                        scheduleAlarms(context, updated)
                    }
                    selectedEventForDetails = null
                    Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                },
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

// Tarjeta del calendario (diseño de la imagen): tarjeta verde oscura con el mes
@Composable
private fun CalendarCard(
    selectedDate: LocalDate,
    events: List<Event>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddReminder: () -> Unit
) {
    val monthLabel = selectedDate.month
        .getDisplayName(TextStyle.FULL, spanishLocale)
        .replaceFirstChar { it.uppercase(spanishLocale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Conserva el verde y el texto blanco también en modo oscuro.
            .keepOriginalColorsInDark()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(CuidaGreen)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Fila del mes con flechas a los lados (chevrons blancos sin borde).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onPreviousWeek),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Semana anterior",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = monthLabel,
                fontFamily = Urbanist,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onNextWeek),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Semana siguiente",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        WeekStrip(
            selectedDate = selectedDate,
            events = events,
            onDateSelected = onDateSelected
        )

        // Botón de añadir recordatorio dentro de la tarjeta (sustituye al FAB).
        Button(
            onClick = onAddReminder,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .semantics { contentDescription = "Añadir Evento" },
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = CuidaGreen
            )
        ) {
            Text(text = "Añadir Evento", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// Tira horizontal con los 7 días de la semana de la fecha seleccionada
@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val days = (0L..6L).map { weekStart.plusDays(it) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { date ->
            WeekDayCell(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == LocalDate.now(),
                hasEvents = events.any { it.occursOn(date) },
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeekDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekdayLabel = date.dayOfWeek
        .getDisplayName(TextStyle.SHORT, spanishLocale)
        .replaceFirstChar { it.uppercase(spanishLocale) }
        .trimEnd('.')
    val description = buildString {
        append(date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", spanishLocale)))
        if (isToday) append(", hoy")
        if (isSelected) append(", seleccionado")
        if (hasEvents) append(", con eventos")
    }

    // Colores para la tarjeta verde: texto blanco y día seleccionado en círculo claro.
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = weekdayLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.85f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = when {
                    isSelected -> CuidaGreenDark
                    isToday -> CuidaGreenLight
                    else -> Color.White
                }
            )
        }
        // Punto indicador de eventos (oculto en el día seleccionado).
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (hasEvents && !isSelected) CuidaGreenLight else Color.Transparent)
        )
    }
}

// Alto mínimo de cada franja de hora del eje temporal.
private val HOUR_ROW_MIN_HEIGHT = 76.dp
// Ancho de la columna izquierda con las etiquetas de hora.
private val HOUR_AXIS_WIDTH = 60.dp
// Ancho fijo de cada caja de recordatorio (se forman en horizontal con scroll).
private val EVENT_BOX_WIDTH = 210.dp

@Composable
private fun DayAgenda(
    selectedDate: LocalDate,
    events: List<Event>,
    completedKeys: Set<String>,
    onEventClick: (Event) -> Unit,
    onToggleCompleted: (Event) -> Unit
) {
    val isToday = selectedDate == LocalDate.now()
    val now = remember(selectedDate) { LocalTime.now() }
    // Fecha grande como título (p. ej. "Viernes, 24").
    val title = selectedDate
        .format(DateTimeFormatter.ofPattern("EEEE d", spanishLocale))
        .replaceFirstChar { it.uppercase(spanishLocale) }

    // Se muestran TODAS las horas del día (00:00–23:00); la lista hace scroll.
    val hours = (0..23).toList()
    val sortedEvents = events.sortedBy { it.time }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // Posición de scroll que deja la hora actual a la vista (~1 hora antes).
    val nowScrollIndex = (now.hour - 1).coerceAtLeast(0)

    // Solo mientras se está scrolleando la lista, la tarjeta de la fecha se
    val scrolled = listState.isScrollInProgress
    // Solo se anima el radio y la sombra (cambios de DIBUJO, baratos). NO se
    val cardCorner by animateDpAsState(targetValue = if (scrolled) 50.dp else 0.dp, label = "cardCorner")
    val cardElevation by animateDpAsState(targetValue = if (scrolled) 3.dp else 0.dp, label = "cardElevation")
    val cardShape = RoundedCornerShape(cardCorner)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = cardElevation, shape = cardShape)
                .clip(cardShape)
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary,
                modifier = Modifier.weight(1f)
            )
            // Botón "Hora actual": lleva la lista a la franja de ahora.
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(CuidaGreenSurface)
                    .clickable(role = Role.Button) {
                        scope.launch { listState.animateScrollToItem(nowScrollIndex) }
                    }
                    .semantics { contentDescription = "Ir a la hora actual" }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = CuidaGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hora actual",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CuidaGreenDark
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Al abrir el día de hoy, centra la hora actual mostrando ~1 hora antes.
        LaunchedEffect(isToday, now.hour) {
            if (isToday) {
                listState.scrollToItem(nowScrollIndex)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            // Margen inferior para que la ultima hora (23:00) no quede tapada.
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(hours) { hour ->
                Column {
                    HourRow(
                        hour = hour,
                        events = sortedEvents.filter { it.time.hour == hour },
                        isCompleted = { event -> completedKeys.contains("${event.id}|$selectedDate") },
                        onEventClick = onEventClick,
                        onToggleCompleted = onToggleCompleted
                    )
                    // Indicador de "ahora" justo bajo la franja de la hora actual.
                    if (isToday && now.hour == hour) {
                        NowIndicator()
                    }
                }
            }
        }
    }
}

// Una franja horaria del eje: etiqueta de hora a la izquierda y, a la derecha,
@Composable
private fun HourRow(
    hour: Int,
    events: List<Event>,
    isCompleted: (Event) -> Boolean,
    onEventClick: (Event) -> Unit,
    onToggleCompleted: (Event) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HOUR_ROW_MIN_HEIGHT),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "%02d:00".format(hour),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextDisabled,
            modifier = Modifier.width(HOUR_AXIS_WIDTH)
        )
        // Las completadas van al final; las pendientes primero. Todas las cajas
        val ordered = events.sortedWith(compareBy({ isCompleted(it) }, { it.time }))
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ordered.forEach { event ->
                EventBox(
                    event = event,
                    isCompleted = isCompleted(event),
                    onClick = { onEventClick(event) },
                    onToggleCompleted = { onToggleCompleted(event) },
                    modifier = Modifier.width(EVENT_BOX_WIDTH)
                )
            }
        }
    }
}

// Indicador de la hora actual: círculo hueco en el eje + línea horizontal.
@Composable
private fun NowIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(HOUR_AXIS_WIDTH),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(CuidaGreen),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(CuidaGreen)
        )
    }
}

// Caja del recordatorio: nombre del evento, su hora y un checkbox para marcarlo
@Composable
private fun EventBox(
    event: Event,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = eventStyleFor(event.name)
    val timeText = event.time.format(DateTimeFormatter.ofPattern("HH:mm"))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(style.container)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "Evento ${event.name} a las $timeText." +
                    (if (isCompleted) " Completado." else "") + " Pulsa para ver y editar."
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = event.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCompleted) CuidaTextSecondary else CuidaTextPrimary,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            )
            Text(
                text = timeText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = style.color
            )
        }
        CompletionCheck(isCompleted = isCompleted, onToggle = onToggleCompleted)
    }
}

// Checkbox circular para marcar un recordatorio como completado.
@Composable
private fun CompletionCheck(isCompleted: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .then(
                if (isCompleted) Modifier.background(CuidaGreen)
                else Modifier.border(2.dp, CuidaGreen, CircleShape)
            )
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .semantics {
                contentDescription = if (isCompleted) "Marcar como no completado" else "Marcar como completado"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Colores de la app para los controles Material de los diálogos.
@Composable
private fun appSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = CuidaGreen,
    checkedBorderColor = CuidaGreen,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = CuidaTextDisabled,
    uncheckedBorderColor = CuidaTextDisabled
)

@Composable
private fun appFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = CuidaGreen,
    selectedLabelColor = Color.White
)

@Composable
private fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CuidaGreen,
    focusedLabelColor = CuidaGreen,
    cursorColor = CuidaGreen
)

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
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Nuevo evento",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del evento") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(18.dp),
                    colors = appTextFieldColors()
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
                        shape = RoundedCornerShape(percent = 50)
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
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it }, colors = appSwitchColors())
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
                                label = { Text(day) },
                                colors = appFilterChipColors()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar alarma", color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it }, colors = appSwitchColors())
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
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// Diálogo de detalle y edición de un evento: muestra el nombre y permite editar
@Composable
fun EventDetailsDialog(
    event: Event,
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onDeleteDay: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val style = eventStyleFor(event.name)
    val context = LocalContext.current

    var description by remember { mutableStateOf(event.description ?: "") }
    var selectedTime by remember { mutableStateOf(event.time) }
    var isRecurring by remember { mutableStateOf(event.isRecurring) }
    var selectedDays by remember { mutableStateOf(event.recurringDays.toSet()) }
    var hasAlarm by remember { mutableStateOf(event.hasAlarm) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
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

                // Descripción editable.
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(18.dp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hora editable.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp), tint = CuidaTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
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
                            .padding(6.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CuidaGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ¿Es recurrente?
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Es recurrente?", color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it }, colors = appSwitchColors())
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Repetir los días:", color = CuidaTextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                                label = { Text(day) },
                                colors = appFilterChipColors()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Activar alarma.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activar alarma", color = CuidaTextPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it }, colors = appSwitchColors())
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSave(
                            event.copy(
                                description = description.ifBlank { null },
                                time = selectedTime,
                                dates = if (isRecurring) emptyList() else event.dates,
                                isRecurring = isRecurring,
                                recurringDays = selectedDays.toList(),
                                hasAlarm = hasAlarm
                            )
                        )
                    },
                    enabled = !isRecurring || selectedDays.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text("Guardar cambios", fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CuidaDivider)
                Spacer(modifier = Modifier.height(8.dp))

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
            }
        }
    }
}
