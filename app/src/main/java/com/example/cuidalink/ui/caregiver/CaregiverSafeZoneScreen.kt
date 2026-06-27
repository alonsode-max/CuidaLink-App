package com.example.cuidalink.ui.caregiver

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.ScreenHeader
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.ui.OsmMap
import com.example.cuidalink.ui.rememberOsmMapController
import org.osmdroid.util.GeoPoint
import kotlin.math.roundToInt

// Centro de la zona segura (SOLO UI / simulado). Sustituir por datos reales.
private val ZONE_CENTER = GeoPoint(40.4168, -3.7038) // Madrid
private const val ZONE_ZOOM = 14.0
private const val MIN_RADIUS = 100f
private const val MAX_RADIUS = 5000f
private const val RESIDENCE_NAME = "Residencia Principal"
private const val RESIDENCE_ADDRESS = "Calle Mayor 123, Madrid"

private enum class ZoneType { VERDE, ROJA }

/** Gestion de zonas seguras: define el area permitida del paciente. */
@Composable
fun CaregiverSafeZoneScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var zoneType by remember { mutableStateOf(ZoneType.VERDE) }
    var radius by remember { mutableFloatStateOf(500f) }
    var exitAlert by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenHeader(
            title = "Zonas Seguras",
            icon = Icons.Filled.LocationOn,
            subtitle = "Áreas permitidas del paciente"
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
                text = "Configure las áreas geográficas permitidas y restringidas para el " +
                    "paciente. Reciba alertas inmediatas si se detecta actividad fuera de los límites.",
                fontSize = 14.sp,
                color = CuidaTextSecondary
            )

            TopActions()
            ZoneTypeCard(selected = zoneType, onSelect = { zoneType = it })
            RadiusCard(radius = radius, onRadiusChange = { radius = it })
            AlertsCard(exitAlert = exitAlert, onToggle = { exitAlert = it })
            MapCard(radius = radius)
        }
    }
}

@Composable
private fun TopActions() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = {},
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp)
                .border(1.dp, CuidaBorderLight, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = CuidaTextPrimary
            )
        ) {
            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Historial", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {},
            modifier = Modifier
                .weight(1.4f)
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CuidaGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Guardar Cambios", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ZoneTypeCard(selected: ZoneType, onSelect: (ZoneType) -> Unit) {
    SectionCard(title = "Tipo de Zona") {
        ZoneOption(
            title = "Zona Verde",
            subtitle = "Área segura permitida",
            icon = Icons.Filled.CheckCircle,
            accent = CuidaGreen,
            surface = CuidaGreenSurface,
            isSelected = selected == ZoneType.VERDE,
            onClick = { onSelect(ZoneType.VERDE) }
        )
        Spacer(Modifier.height(12.dp))
        ZoneOption(
            title = "Zona Roja",
            subtitle = "Área peligrosa o prohibida",
            icon = Icons.Filled.Cancel,
            accent = CuidaRed,
            surface = CuidaRedSurface,
            isSelected = selected == ZoneType.ROJA,
            onClick = { onSelect(ZoneType.ROJA) }
        )
    }
}

@Composable
private fun ZoneOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    surface: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) surface else CuidaSurfaceMuted)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else CuidaBorderLight,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CuidaTextPrimary)
            Text(subtitle, fontSize = 13.sp, color = CuidaTextSecondary)
        }
    }
}

@Composable
private fun RadiusCard(radius: Float, onRadiusChange: (Float) -> Unit) {
    SectionCard(title = "Radio de Zona", trailing = {
        Text(
            text = "${radius.roundToInt()} m",
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(CuidaGreenSurface)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaGreenDark
        )
    }) {
        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = MIN_RADIUS..MAX_RADIUS,
            colors = SliderDefaults.colors(
                thumbColor = CuidaGreen,
                activeTrackColor = CuidaGreen,
                inactiveTrackColor = CuidaSurfaceMuted
            )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("100m", modifier = Modifier.weight(1f), fontSize = 12.sp, color = CuidaTextSecondary)
            Text("5km", fontSize = 12.sp, color = CuidaTextSecondary)
        }
    }
}

@Composable
private fun AlertsCard(exitAlert: Boolean, onToggle: (Boolean) -> Unit) {
    SectionCard(title = "Configuración de Alertas") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CuidaRedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = CuidaRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Alerta de Salida", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CuidaTextPrimary)
                Text("Activar sirena en móvil", fontSize = 13.sp, color = CuidaTextSecondary)
            }
            Switch(
                checked = exitAlert,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CuidaGreen
                )
            )
        }
    }
}

// Mapa con el círculo de la zona segura (radio ligado al slider) y controles.
@Composable
private fun MapCard(radius: Float) {
    val mapController = rememberOsmMapController()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        OsmMap(
            center = ZONE_CENTER,
            zoom = ZONE_ZOOM,
            controller = mapController,
            circleRadiusMeters = radius.toDouble(),
            circleStroke = CuidaGreen,
            circleFill = CuidaGreen.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxSize()
        )

        // Marcador central de la zona segura.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CuidaGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Zona Segura: Casa",
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(percent = 50))
                    .clip(RoundedCornerShape(percent = 50))
                    .background(CuidaGreenDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Barra de búsqueda (visual).
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(2.dp, RoundedCornerShape(percent = 50))
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = CuidaTextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Buscar dirección o lugar...", fontSize = 14.sp, color = CuidaTextSecondary)
        }

        // Controles a la izquierda (centrar, acercar, alejar).
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapControl(Icons.Filled.MyLocation, "Centrar") {
                mapController.recenter(ZONE_CENTER, ZONE_ZOOM)
            }
            MapControl(Icons.Filled.Add, "Acercar") {
                mapController.zoomIn()
            }
            MapControl(Icons.Filled.Remove, "Alejar") {
                mapController.zoomOut()
            }
        }

        } // cierra el Box del mapa

        // Tarjeta de residencia, ahora DEBAJO del mapa (ya no superpuesta, no
        ResidenceCard(
            radius = radius,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ResidenceCard(radius: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CuidaGreen, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(RESIDENCE_NAME, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CuidaTextPrimary)
            Text(RESIDENCE_ADDRESS, fontSize = 12.sp, color = CuidaTextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Radio: ${radius.roundToInt()}m",
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(CuidaGreenSurface)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaGreenDark
            )
        }
        Text(
            text = "Editar",
            modifier = Modifier.clickable(onClick = {}),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaGreen
        )
    }
}

@Composable
private fun MapControl(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = CuidaTextPrimary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontFamily = Urbanist,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            trailing?.invoke()
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}
