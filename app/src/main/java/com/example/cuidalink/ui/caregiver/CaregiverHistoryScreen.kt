package com.example.cuidalink.ui.caregiver

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaAmber
import com.example.cuidalink.ui.theme.CuidaAmberSurface
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist

// Una entrada del historial (SOLO UI / simulada).
private data class HistoryEntry(
    val time: String,
    val title: String,
    val detail: String,
    val icon: ImageVector,
    val tint: Color,
    val surface: Color
)

/** Historial de actividad del paciente para el cuidador. */
@Composable
fun CaregiverHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val entries = listOf(
        HistoryEntry(
            time = "Hace 5 min",
            title = "Ubicación actualizada",
            detail = "Near Central Park, West Ave.",
            icon = Icons.Filled.LocationOn,
            tint = CuidaGreenDark,
            surface = CuidaGreenSurface
        ),
        HistoryEntry(
            time = "Hoy, 10:30",
            title = "Medicación tomada",
            detail = "Donepezilo 10mg",
            icon = Icons.Filled.Medication,
            tint = CuidaGreenDark,
            surface = CuidaGreenSurface
        ),
        HistoryEntry(
            time = "Hoy, 09:45",
            title = "Juego completado",
            detail = "Juego de memoria · 8/10 aciertos",
            icon = Icons.Filled.CheckCircle,
            tint = CuidaGreenDark,
            surface = CuidaGreenSurface
        ),
        HistoryEntry(
            time = "Ayer, 18:20",
            title = "Salió de la zona segura",
            detail = "Regresó a los 12 min",
            icon = Icons.Filled.Warning,
            tint = CuidaAmber,
            surface = CuidaAmberSurface
        ),
        HistoryEntry(
            time = "Ayer, 16:05",
            title = "Alerta SOS",
            detail = "Cancelada por el paciente",
            icon = Icons.Filled.Warning,
            tint = CuidaRed,
            surface = CuidaRedSurface
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CaregiverTopBar(title = "Historial", onBack = onBack, subtitle = "Actividad reciente")

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
            entries.forEach { HistoryRow(it) }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
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
                .background(entry.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = entry.tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                fontFamily = Urbanist,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = entry.detail,
                fontSize = 14.sp,
                color = CuidaTextSecondary
            )
        }
        Text(
            text = entry.time,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextSecondary
        )
    }
}
