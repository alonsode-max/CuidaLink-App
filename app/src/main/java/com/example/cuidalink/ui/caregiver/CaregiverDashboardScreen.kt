package com.example.cuidalink.ui.caregiver

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.ScreenHeader
import com.example.cuidalink.ui.notifyZoneExit
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.ui.theme.keepOriginalColorsInDark
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.OsmMap
import com.example.cuidalink.ui.rememberOsmMapController
import com.example.cuidalink.viewmodel.CaregiverProfileViewModel
import com.example.cuidalink.viewmodel.ProfileUiState
import org.osmdroid.util.GeoPoint

// Texto informativo de la última sincronización (la telemetría real llega por Realtime).
private const val LAST_UPDATED = "Justo ahora"

// Ubicación por defecto del mapa cuando el paciente aún no ha reportado posición.
private val DEFAULT_LOCATION = GeoPoint(41.4145, 2.2244)
private const val DEFAULT_ZOOM = 14.0

/** Home del cuidador: header verde + monitoreo del paciente (datos reales del backend). */
@Composable
fun CaregiverDashboardScreen(
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onConfigureZone: () -> Unit = {},
    onNeedsLinking: () -> Unit = {},
    viewModel: CaregiverProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val data = (state as? ProfileUiState.Success)?.data
    val isLinked = data?.isLinked == true

    // Aviso de salida de zona segura: notificación del sistema + banner en pantalla.
    val outsideZone by viewModel.outsideZone.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(outsideZone) {
        if (outsideZone) notifyZoneExit(context, data?.patientName ?: "El paciente")
    }

    // Si ya sabemos que el cuidador no tiene paciente vinculado, no tiene sentido
    // mostrar el dashboard: lo mandamos a la pantalla de vinculación.
    val needsLinking = state is ProfileUiState.Success && !isLinked
    LaunchedEffect(needsLinking) {
        if (needsLinking) onNeedsLinking()
    }

    val patientName = when {
        state is ProfileUiState.Loading -> "Cargando…"
        isLinked -> data?.patientName ?: "Paciente"
        else -> "Sin paciente vinculado"
    }
    val patientInitials = initialsOf(if (isLinked) data?.patientName else null)

    val hasLocation = isLinked && data?.patientLat != null && data.patientLng != null
    val patientLocation = if (hasLocation) {
        GeoPoint(data!!.patientLat!!, data.patientLng!!)
    } else {
        DEFAULT_LOCATION
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenHeader(
            title = patientName,
            icon = Icons.Filled.Person,
            subtitle = if (isLinked) "Paciente monitoreado" else "Vincula un paciente para empezar",
            trailing = { OnlinePill() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pasos de hoy: ${data?.steps ?: 0} • Última actualización: $LAST_UPDATED",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
            if (outsideZone) {
                ZoneExitBanner()
            }
            // El aviso sigue saliendo hasta que exista al menos una geovalla.
            if (isLinked && data?.hasSafeZone != true) {
                NoZoneConfiguredBanner(onConfigureZone = onConfigureZone)
            }
            ActionButtonsRow(
                onOpenHistory = onOpenHistory,
                onOpenProfile = onOpenProfile,
                onSync = { viewModel.loadCurrentCaregiver() }
            )
            LocationCard(
                location = patientLocation,
                initials = patientInitials,
                hasLocation = hasLocation
            )
            BatteryCard(percent = data?.batteryPercent)
            LastActivityCard(
                activity = data?.lastActivity,
                minutesPlayed = data?.minutesPlayed
            )
        }
    }
}

/** Iniciales (hasta 2) a partir del nombre del paciente, para el pin del mapa. */
private fun initialsOf(name: String?): String =
    name?.trim()
        ?.split(Regex("\\s+"))
        ?.filter { it.isNotEmpty() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?.ifEmpty { "?" }
        ?: "?"

// Tarjeta de batería del dispositivo con anillo de progreso.
@Composable
private fun BatteryCard(percent: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Batería del dispositivo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextSecondary
            )
            Text(
                text = percent?.let { "$it%" } ?: "—",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = if (percent != null) "Batería del móvil del paciente" else "Sin datos aún",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
        }
        BatteryRing(progress = (percent ?: 0) / 100f)
    }
}

@Composable
private fun BatteryRing(progress: Float) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = CuidaGreenSurface,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = CuidaGreen,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = null,
            tint = CuidaGreen,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Tarjeta verde con la última actividad del paciente.
@Composable
private fun LastActivityCard(activity: String?, minutesPlayed: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            // Mantiene el verde y el texto blanco también en modo oscuro.
            .keepOriginalColorsInDark()
            .background(
                Brush.linearGradient(listOf(CuidaGreenDark, CuidaGreen))
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ÚLTIMA ACTIVIDAD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Text(
            text = activity?.let { "Jugó '$it'" } ?: "Sin actividad reciente",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = if (activity != null) "Última actividad registrada del paciente."
                else "Aún no ha jugado ninguna sesión.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = minutesPlayed?.let { "$it min jugados en total" } ?: "0 min jugados",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Button(
                onClick = {},
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = CuidaGreenDark
                )
            ) {
                Text("Ver detalles", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Banner rojo cuando el paciente sale de su zona segura.
@Composable
private fun ZoneExitBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CuidaRedSurface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = CuidaRed,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Fuera de la zona segura!",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaRed
            )
            Text(
                text = "El paciente ha salido del área permitida.",
                fontSize = 13.sp,
                color = CuidaRed
            )
        }
    }
}

/** Aviso cuando aún no se ha configurado una zona segura; lleva a la pantalla de zonas. */
@Composable
private fun NoZoneConfiguredBanner(onConfigureZone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CuidaGreenSurface)
            .clickable(role = Role.Button, onClick = onConfigureZone)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = CuidaGreenDark,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sin zona segura configurada",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
            Text(
                text = "Toca aquí para configurar una y recibir avisos si el paciente sale.",
                fontSize = 13.sp,
                color = CuidaGreenDark
            )
        }
    }
}

@Composable
private fun OnlinePill() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(CuidaGreen)
        )
        Text(
            text = "En línea",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaGreenDark
        )
    }
}

// Fila de acciones: History (contorno), Ver Perfil (claro) y Sync Now (verde).
@Composable
private fun ActionButtonsRow(
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onSync: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Historial",
            icon = Icons.Filled.History,
            container = Color.White,
            content = CuidaTextPrimary,
            borderColor = CuidaBorderLight,
            onClick = onOpenHistory
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Ver Perfil",
            icon = Icons.Filled.Person,
            container = CuidaGreenSurface,
            content = CuidaGreenDark,
            borderColor = null,
            onClick = onOpenProfile
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "Sincronizar",
            icon = Icons.Filled.Sync,
            container = CuidaGreen,
            content = Color.White,
            borderColor = null,
            onClick = onSync
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    borderColor: Color?,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 64.dp)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .semantics { contentDescription = label },
        shape = shape,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

// Tarjeta con el mapa de ubicación en tiempo real.
@Composable
private fun LocationCard(
    location: GeoPoint,
    initials: String,
    hasLocation: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = CuidaGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ubicación en tiempo real",
                modifier = Modifier.weight(1f),
                fontFamily = Urbanist,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            GpsBadge()
        }

        MapView(location = location, initials = initials, hasLocation = hasLocation)
    }
}

@Composable
private fun GpsBadge() {
    Text(
        text = "Señal GPS: Fuerte",
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaSurfaceMuted)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = CuidaTextSecondary
    )
}

// Mapa con la ubicacion del paciente fijada al centro.
@Composable
private fun MapView(
    location: GeoPoint,
    initials: String,
    hasLocation: Boolean
) {
    val mapController = rememberOsmMapController()
    val locationLabel = if (hasLocation) {
        "Lat %.4f, Lng %.4f".format(location.latitude, location.longitude)
    } else {
        "Ubicación no disponible aún"
    }

    // Cuando llega una nueva ubicación en tiempo real, seguimos al paciente en el mapa.
    LaunchedEffect(location.latitude, location.longitude, hasLocation) {
        if (hasLocation) mapController.recenter(location, DEFAULT_ZOOM)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        OsmMap(
            center = location,
            zoom = DEFAULT_ZOOM,
            controller = mapController,
            modifier = Modifier.fillMaxSize()
        )

        // Avatar del paciente centrado (marca su ubicación).
        PatientAvatarPin(
            initials = initials,
            modifier = Modifier.align(Alignment.Center)
        )

        // Etiqueta de la zona, abajo al centro.
        Text(
            text = locationLabel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .shadow(2.dp, RoundedCornerShape(percent = 50))
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextPrimary
        )

        // Controles a la derecha: acercar, alejar y recentrar en el paciente.
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapControlButton(icon = Icons.Filled.Add, description = "Acercar") {
                mapController.zoomIn()
            }
            MapControlButton(icon = Icons.Filled.Remove, description = "Alejar") {
                mapController.zoomOut()
            }
            MapControlButton(icon = Icons.Filled.MyLocation, description = "Centrar en el paciente") {
                mapController.recenter(location, DEFAULT_ZOOM)
            }
        }
    }
}

@Composable
private fun PatientAvatarPin(initials: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
        }
    }
}

@Composable
private fun MapControlButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .size(48.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CuidaTextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
