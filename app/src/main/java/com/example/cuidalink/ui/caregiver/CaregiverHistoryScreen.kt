package com.example.cuidalink.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.remote.EventCompletionRow
import com.example.cuidalink.model.remote.LocationHistoryRow
import com.example.cuidalink.ui.openInGoogleMaps
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es", "ES"))

private val TIMESTAMP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale("es", "ES"))

/** Historial de tareas completadas del paciente, para el cuidador (datos reales). */
@Composable
fun CaregiverHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CaregiverTopBar(title = "Historial", onBack = onBack, subtitle = "Tareas completadas")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 18.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LocationHistoryCard(
                locations = locations,
                onOpenPoint = { point -> openInGoogleMaps(context, point.lat, point.lng) }
            )

            when {
                isLoading -> Text(
                    text = "Cargando historial…",
                    fontSize = 14.sp,
                    color = CuidaTextSecondary
                )
                items.isEmpty() -> Text(
                    text = "Aún no hay tareas completadas. Cuando el paciente marque un " +
                        "recordatorio como hecho, aparecerá aquí.",
                    fontSize = 15.sp,
                    color = CuidaTextSecondary
                )
                else -> items.forEach { HistoryRow(it) }
            }
        }
    }
}

/**
 * Tarjeta desplegable "Historial de ubicación": al pulsarla muestra la lista de las
 * últimas ubicaciones donde se pudo localizar al paciente. Cada punto abre Google Maps.
 */
@Composable
private fun LocationHistoryCard(
    locations: List<LocationHistoryRow>,
    onOpenPoint: (LocationHistoryRow) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CuidaGreenSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = CuidaGreenDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historial de ubicación",
                    fontFamily = Urbanist,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
                Text(
                    text = if (locations.isEmpty()) {
                        "Aún no hay ubicaciones registradas."
                    } else {
                        "${locations.size} ubicaciones · toca para ${if (expanded) "ocultar" else "ver"}"
                    },
                    fontSize = 14.sp,
                    color = CuidaTextSecondary
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = CuidaTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        if (expanded && locations.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                locations.forEach { point ->
                    LocationPointRow(point = point, onClick = { onOpenPoint(point) })
                }
            }
        }
    }
}

/** Un punto del historial: fecha/hora + coordenadas; abre Google Maps al tocarlo. */
@Composable
private fun LocationPointRow(point: LocationHistoryRow, onClick: () -> Unit) {
    val timeText = formatTimestamp(point.createdAt)
    val coords = "%.5f, %.5f".format(Locale.US, point.lat, point.lng)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CuidaGreenSurface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = CuidaGreenDark,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaTextPrimary
            )
            Text(
                text = coords,
                fontSize = 12.sp,
                color = CuidaTextSecondary
            )
        }
        Text(
            text = "Ver en Maps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaGreenDark
        )
    }
}

/** Convierte el timestamp ISO del backend a texto legible; si falla, lo muestra tal cual. */
private fun formatTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return "Ubicación registrada"
    return runCatching {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()).format(TIMESTAMP_FORMAT)
    }.getOrDefault(iso)
}

@Composable
private fun HistoryRow(entry: EventCompletionRow) {
    val dateText = runCatching { LocalDate.parse(entry.date).format(DATE_FORMAT) }
        .getOrDefault(entry.date)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = CuidaGreenDark,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.eventName,
                fontFamily = Urbanist,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "Tarea completada",
                fontSize = 14.sp,
                color = CuidaTextSecondary
            )
        }
        Text(
            text = dateText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextSecondary
        )
    }
}
