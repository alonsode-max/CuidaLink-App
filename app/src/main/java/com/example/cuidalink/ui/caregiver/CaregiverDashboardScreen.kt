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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.ScreenHeader
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.ui.theme.keepOriginalColorsInDark
import com.example.cuidalink.ui.OsmMap
import com.example.cuidalink.ui.rememberOsmMapController
import org.osmdroid.util.GeoPoint

// Datos del paciente (SOLO UI / simulados); sustituir al integrar backend.
private const val PATIENT_NAME = "Maria Garcia"
private const val DEVICE_ID = "#MG-2023-XJ"
private const val LAST_UPDATED = "Just now"
private const val LOCATION_LABEL = "Near Central Park, West Ave."
private const val BATTERY_PERCENT = 85
private const val BATTERY_HOURS = 12
private const val LAST_ACTIVITY = "Family Faces"
private const val LAST_ACTIVITY_AGO = 10

// Ubicación simulada del paciente (mientras no hay backend de localización).
private val PATIENT_LOCATION = GeoPoint(41.4145, 2.2244)
private const val DEFAULT_ZOOM = 14.0

/** Home del cuidador: header verde + monitoreo del paciente. */
@Composable
fun CaregiverDashboardScreen(
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenHeader(
            title = PATIENT_NAME,
            icon = Icons.Filled.Person,
            subtitle = "Paciente monitoreado",
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
                text = "Última actualización: $LAST_UPDATED • Dispositivo: $DEVICE_ID",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
            ActionButtonsRow(onOpenHistory = onOpenHistory, onOpenProfile = onOpenProfile)
            LocationCard()
            BatteryCard()
            LastActivityCard()
        }
    }
}

// Tarjeta de batería del dispositivo con anillo de progreso.
@Composable
private fun BatteryCard() {
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
                text = "Device Battery",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextSecondary
            )
            Text(
                text = "$BATTERY_PERCENT%",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "Approx. ${BATTERY_HOURS}h remaining",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
        }
        BatteryRing(progress = BATTERY_PERCENT / 100f)
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
private fun LastActivityCard() {
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
                text = "LAST ACTIVITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Text(
            text = "Played '$LAST_ACTIVITY'",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Memory game session completed successfully.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$LAST_ACTIVITY_AGO min ago",
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
                Text("View Details", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
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
            text = "Online",
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
    onOpenProfile: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionButton(
            modifier = Modifier.weight(1f),
            label = "History",
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
            label = "Sync Now",
            icon = Icons.Filled.Sync,
            container = CuidaGreen,
            content = Color.White,
            borderColor = null
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
private fun LocationCard() {
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
                text = "Real-time Location",
                modifier = Modifier.weight(1f),
                fontFamily = Urbanist,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            GpsBadge()
        }

        MapView()
    }
}

@Composable
private fun GpsBadge() {
    Text(
        text = "GPS Signal: Strong",
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
private fun MapView() {
    val mapController = rememberOsmMapController()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        OsmMap(
            center = PATIENT_LOCATION,
            zoom = DEFAULT_ZOOM,
            controller = mapController,
            modifier = Modifier.fillMaxSize()
        )

        // Avatar del paciente centrado (marca su ubicación).
        PatientAvatarPin(
            modifier = Modifier.align(Alignment.Center)
        )

        // Etiqueta de la zona, abajo al centro.
        Text(
            text = LOCATION_LABEL,
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
                mapController.recenter(PATIENT_LOCATION, DEFAULT_ZOOM)
            }
        }
    }
}

@Composable
private fun PatientAvatarPin(modifier: Modifier = Modifier) {
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
                text = "MG",
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
