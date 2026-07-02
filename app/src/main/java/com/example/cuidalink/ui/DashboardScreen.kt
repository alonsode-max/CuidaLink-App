package com.example.cuidalink.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.Event
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.LocalDarkThemeActive
import com.example.cuidalink.ui.theme.keepOriginalColorsInDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.AccountViewModel
import com.example.cuidalink.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

// Dosis por defecto cuando el evento de medicación no trae descripción
private const val DEFAULT_DOSE = "1 comprimido"

private const val SECONDS_PER_MINUTE = 60
private const val MILLIS_PER_SECOND = 1_000L

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", spanishLocale)

// Frases para la sección "Frase del día". Para añadir más, basta con agregar
private data class DailyPhrase(val text: String, val author: String)

private val dailyPhrases = listOf(
    DailyPhrase("\"Cada día trae consigo una nueva oportunidad de sonreír.\"", "— Con cariño, tu familia"),
    DailyPhrase("\"Lo importante no es lo que pasa, sino cómo lo recibes.\"", "— Con cariño, tu familia"),
    DailyPhrase("\"Hoy es un buen día para hacer algo que te guste.\"", "— Con cariño, tu familia")
)

/** Home del paciente: cabecera de saludo y tarjetas de accion. */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = viewModel(),
    accountViewModel: AccountViewModel = viewModel(),
    onOpenCalendar: () -> Unit = {},
    onPlay: () -> Unit = {}
) {
    val events by calendarViewModel.events.collectAsState()
    val account by accountViewModel.account.collectAsState()
    val context = LocalContext.current
    // "Llamar a casa": marca el teléfono de contacto guardado al registrarse.
    val onCallHome: () -> Unit = {
        val phone = account?.emergencyPhone?.takeIf { it.isNotBlank() }
        if (phone != null) {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } else {
            Toast.makeText(
                context,
                "No hay un teléfono de contacto configurado.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    // Primer nombre del paciente logueado (p. ej. "Carlos Tumbaco" -> "Carlos").
    val firstName = account?.name
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.firstOrNull()
        ?.takeIf { it.isNotEmpty() }
        ?: ""
    val today = LocalDate.now()
    val now = remember { LocalTime.now() }

    // Tomas de medicación de hoy todavía pendientes (hora >= ahora).
    val pendingMedications = remember(events) {
        events
            .filter { it.occursOn(today) && isMedicationEvent(it.name) && !it.time.isBefore(now) }
            .sortedBy { it.time }
    }
    // Tomas de hoy ya completadas (hora < ahora): se usan para el progreso real
    val completedMedicationCount = remember(events) {
        events.count { it.occursOn(today) && isMedicationEvent(it.name) && it.time.isBefore(now) }
    }
    val nextMedication = pendingMedications.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        PatientHeader(patientName = firstName)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // "Parte de abajo" (donde van las cartas): esquinas superiores
                .offset(y = (-28).dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp)
                // Margen inferior amplio para que las tarjetas puedan
                .padding(top = 15.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MedicationCard(
            medication = nextMedication,
            pendingCount = pendingMedications.size,
            completedCount = completedMedicationCount,
            onOpenCalendar = onOpenCalendar
        )
            BentoBox(onPlay = onPlay, onCallHome = onCallHome)
        }
    }
}

// Cabecera sólida en verde azulado con esquinas inferiores redondeadas. Texto
@Composable
private fun PatientHeader(patientName: String) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    // Refresca la hora justo al cambiar de minuto.
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            delay(MILLIS_PER_SECOND * (SECONDS_PER_MINUTE - now.second))
        }
    }
    val (greeting, isDay) = when (now.hour) {
        in 6..11 -> "Buenos días" to true
        in 12..19 -> "Buenas tardes" to true
        else -> "Buenas noches" to false
    }
    val dateText = remember {
        // Cada palabra con inicial mayúscula: "Sábado, 13 De Junio".
        LocalDate.now().format(dateFormatter)
            .split(' ')
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase(spanishLocale) } }
    }
    val timeText = now.format(timeFormatter)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Sin altura fija: el header se ajusta a su contenido.
            .keepOriginalColorsInDark()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF17A34A),
                        Color(0xFF17A34A),
                        Color(0xFF17A34A),
                        // En oscuro funde hacia gris oscuro.
                        if (LocalDarkThemeActive.current) Color(0xFF262729) else Color(0xFFE4E5E4)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // Margen inferior amplio: deja espacio verde bajo las píldoras
                .padding(top = 24.dp, bottom = 52.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "$greeting. Hola, $patientName. $dateText"
                },
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Fila 1: saludo con icono de sol/luna y píldora de hora.
            Row(
                // 1. Hace que la fila ocupe todo el ancho de la pantalla
                modifier = Modifier.fillMaxWidth(),
                // 2. Centra el contenido verticalmente dentro de la fila
                verticalAlignment = Alignment.CenterVertically,
                // 3. Centra el contenido horizontalmente dentro de la fila
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isDay) HugeIcons.Sun else HugeIcons.Moon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = greeting,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            // Fila 2: "Hola" + nombre con la foto de perfil al lado.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola",
                        fontFamily = Urbanist,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = patientName,
                        fontFamily = Urbanist,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                // Avatar bajado respecto al nombre.
                ProfileAvatar(
                    patientName = patientName,
                    modifier = Modifier.offset(y = 44.dp, x = -10.dp).size(100.dp)
                )
            }
            // Fila 3: fecha en píldora con icono de corazón.
            Row {
                TranslucentPill {
                    Icon(
                        imageVector = HugeIcons.Heart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

            }
            Spacer(modifier = Modifier.padding(top = 1.dp))
            TranslucentPill {
                Text(
                    text = timeText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// Frase del día: solo texto, sin tarjeta ni borde. Cambia cada día recorriendo
@Composable
private fun DailyQuote() {
    val phrase = remember { dailyPhrases[LocalDate.now().dayOfYear % dailyPhrases.size] }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 10.dp, bottom = 20.dp)
    ) {
        // Barrita vertical de acento a la izquierda de la frase.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(CuidaGreen)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "FRASE DEL DÍA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phrase.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phrase.author,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = CuidaTextSecondary
            )
        }
    }
}

// Píldora translúcida reutilizable para la hora y la fecha de la cabecera.
@Composable
private fun TranslucentPill(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// Avatar de perfil para el header (mismas iniciales que en Perfil).
@Composable
private fun ProfileAvatar(patientName: String, modifier: Modifier = Modifier) {
    // Cuadrada con esquinas redondeadas (en vez de círculo).
    val avatarShape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(avatarShape)
            .background(CuidaGreenSurface)
            .border(3.dp, Color.White, avatarShape)
            .semantics { contentDescription = "Foto de perfil de $patientName" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = patientName.take(1).uppercase(),
            fontFamily = Urbanist,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaGreen
        )
    }
}

// Tarjeta de medicación (diseño de la foto): fondo verde claro, botón que abre
@Composable
private fun MedicationCard(
    medication: Event?,
    pendingCount: Int,
    completedCount: Int,
    onOpenCalendar: () -> Unit
) {
    val title = medication?.name ?: "Todo en orden"
    val subtitle = if (medication != null) {
        "A las ${medication.time.format(timeFormatter)}"
    } else {
        "No tienes tomas pendientes\npor ahora"
    }
    val description = "Medicación. $title. $subtitle. $pendingCount pendientes."

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Medicación",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CuidaTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = CuidaTextSecondary
                )

            }
            PendingGauge(
                pendingCount = pendingCount,
                completedCount = completedCount,
                modifier = Modifier.align(Alignment.CenterVertically).offset(x = -12.dp)
            )
        }
        // Botón de redirección al calendario, superpuesto arriba a la derecha
        RedirectButton(
            onClick = onOpenCalendar,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = -12.dp)
        )
    }
}

// Botón circular que abre el calendario (flecha de redirección).
@Composable
private fun RedirectButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(CuidaGreen, CuidaGreen)))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Abrir calendario" },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = HugeIcons.ArrowUpRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
    }
}

// Chip pequeño verde (Salud, Dosis).
@Composable
private fun MedChip(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaGreen)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

// Medidor semicircular con el número de tomas pendientes y un icono de pastilla.
@Composable
private fun PendingGauge(
    pendingCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    // El arco representa el progreso real del día: completadas / total. Con 1
    val total = pendingCount + completedCount
    val progress = if (total > 0) completedCount.toFloat() / total else 0f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(width = 104.dp, height = 54.dp)) {
                val sw = 12.dp.toPx()
                val d = size.width - sw
                val topLeft = Offset(sw / 2f, sw / 2f)
                val arcSize = Size(d, d)
                // Pista de fondo (todo el semicírculo) en verde claro.
                drawArc(
                    color = CuidaGreenSurface,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                // Progreso real con degradado verde.
                if (progress > 0f) {
                    drawArc(
                        brush = Brush.horizontalGradient(listOf(CuidaGreenDark, CuidaGreen)),
                        startAngle = 180f,
                        sweepAngle = 180f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = sw, cap = StrokeCap.Round)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = HugeIcons.Medicine,
                    contentDescription = null,
                    tint = CuidaGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pendingCount.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
            }
        }
        Text(
            text = "Pendientes",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextSecondary
        )
    }
}

// Rejilla "Bento Box": un Row con dos columnas de igual ancho y misma altura
@Composable
private fun BentoBox(onPlay: () -> Unit, onCallHome: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna izquierda: tarjeta pequeña arriba + grande abajo.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BentoInfoCard(
                modifier = Modifier.weight(0.4f),
                icon = HugeIcons.User,
                title = "Llamar",
                subtitle = "Hablar con casa",
                onClick = onCallHome
            )
            BentoBatteryCard(modifier = Modifier.weight(0.6f))
        }
        // Columna derecha: tarjeta grande de pasos arriba + pequeña abajo.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BentoStepsCard(modifier = Modifier.weight(0.6f))
            BentoInfoCard(
                modifier = Modifier.weight(0.4f),
                icon = HugeIcons.Brain,
                title = "A jugar",
                subtitle = "Entrena tu mente",
                onClick = onPlay
            )
        }
    }
}

// Estilo base de tarjeta Bento: blanca, muy redondeada y con sombra tenue.
private fun Modifier.bentoCard(): Modifier = this
    .fillMaxSize()
    .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp))
    .clip(RoundedCornerShape(24.dp))
    .background(Color.White)

// Tarjeta pequeña: icono al lado del texto (sin fondo de badge). Opcionalmente
@Composable
private fun BentoInfoCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = modifier
        .bentoCard()
        .let { base -> if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base }
        .padding(16.dp)
        .semantics(mergeDescendants = true) {
            contentDescription = "$title, $subtitle"
        }
    Row(
        modifier = cardModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CuidaGreen,
            modifier = Modifier.size(30.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = CuidaTextSecondary
            )
        }
    }
}

// Tarjeta grande de batería (diseño de la imagen): fondo en degradado verde,
@Composable
private fun BentoBatteryCard(modifier: Modifier) {
    val percent = rememberBatteryLevel()
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            // Conserva el verde y el texto blanco también en modo oscuro.
            .keepOriginalColorsInDark()
            .shadow(elevation = 3.dp, shape = shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(CuidaGreen, CuidaGreenSurface)))
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Batería al $percent por ciento"
            },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = HugeIcons.Flash,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Batería",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Text(
            text = "$percent%",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        BatteryGauge(percent = percent)
    }
}

// Lee el nivel de batería real del dispositivo y se actualiza cuando cambia,
@Composable
private fun rememberBatteryLevel(): Int {
    val context = LocalContext.current
    var level by remember { mutableStateOf(currentBatteryLevel(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent ?: return
                val raw = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (raw >= 0 && scale > 0) {
                    level = (raw * 100 / scale).coerceIn(0, 100)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return level
}

// Lectura puntual del nivel de batería (para el valor inicial).
private fun currentBatteryLevel(context: Context): Int {
    val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val capacity = manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    return if (capacity in 0..100) capacity else 0
}

// Medidor de batería: barras verticales dentro de una píldora blanca; se pintan
@Composable
private fun BatteryGauge(percent: Int) {
    val totalBars = 16
    val filledBars = (percent / 100f * totalBars).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalBars) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(22.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (index < filledBars) CuidaGreen else CuidaGreenSurface)
            )
        }
    }
}

// Tarjeta de pasos: titulo, cifra grande y progreso.
@Composable
private fun BentoStepsCard(modifier: Modifier) {
    val steps = rememberStepCount()
    val stepsLabel = if (steps >= 1000) "%.1fk".format(steps / 1000f) else steps.toString()
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(elevation = 3.dp, shape = shape)
            .clip(shape)
            .background(Color.White)
            .drawBehind {
                // Brillo radial centrado en el borde inferior de la tarjeta.
                val glowRadius = size.width * 1.05f
                val center = Offset(size.width / 2f, size.height + glowRadius * 0.25f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CuidaGreenDark,
                            CuidaGreen.copy(alpha = 0.85f),
                            CuidaGreenSurface.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
            }
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Tus pasos. $steps pasos hoy"
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tus pasos",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextPrimary,
                modifier = Modifier.weight(1f)
            )

        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stepsLabel,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(
            text = "pasos hoy",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextSecondary
        )
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp,
    iconSize: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun InfoPill(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = CuidaGreenDark,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaGreenSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
