package com.example.cuidalink.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Pantalla "Ayuda en camino": confirma que el contacto fue avisado.

private val ALERT_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Pantalla de emergencia: avisa al contacto principal y permite cancelar. */
@Composable
fun EmergencyHelpScreen(
    modifier: Modifier = Modifier,
    contactName: String = "Lucía",
    onCall: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    // La hora en que se activó la alerta se fija una sola vez al entrar.
    val alertTime = remember { LocalTime.now().format(ALERT_TIME_FORMAT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AlertBanner(time = alertTime)

        // Bloque central desplazable: si la pantalla es pequeña el contenido se
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlertIcon()
            ReassuranceText(contactName = contactName)
            CalmGuideCard()
            LiveMapPlaceholder()
            Spacer(modifier = Modifier.height(4.dp))
        }

        EmergencyActions(onCall = onCall, onCancel = onCancel)
    }
}

// Banda superior roja a todo el ancho: deja claro que la alerta está activa.
@Composable
private fun AlertBanner(time: String) {
    Text(
        text = "ALERTA ACTIVA · $time",
        modifier = Modifier
            .fillMaxWidth()
            .background(CuidaRed)
            .padding(top = 14.dp, bottom = 10.dp),
        textAlign = TextAlign.Center,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.5.sp
    )
}

// Icono de alerta: círculo rojo dentro de un halo rojo claro.
@Composable
private fun AlertIcon() {
    Box(
        modifier = Modifier
            .size(98.dp)
            .clip(CircleShape)
            .background(CuidaRedSurface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(CuidaRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS",
                fontFamily = Urbanist,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ReassuranceText(contactName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "No te muevas.\nLa ayuda viene en camino.",
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$contactName ya está avisada y va hacia ti.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = CuidaTextSecondary
        )
    }
}

// Audioguía de calma: tarjeta verde con reproducción y barra de progreso.
@Composable
private fun CalmGuideCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CuidaGreenSurface)
            .padding(14.dp)
            .semantics {
                contentDescription = "Guía de calma. Respira conmigo, audio de dos minutos diez segundos"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CuidaGreenAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Guía de calma",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
            Text(
                text = "Respira conmigo · 2:10",
                fontSize = 12.sp,
                color = CuidaGreenSubtle
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Barra de progreso: pista blanca con el 35% relleno en verde.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CuidaGreenAccent)
                )
            }
        }
    }
}

// Marcador de posición del mapa en vivo: fondo de rayas diagonales con un punto
@Composable
private fun LiveMapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CuidaSurfaceFaint)
            .semantics { contentDescription = "Mapa con tu ubicación en tiempo real" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Rayas diagonales a 45° imitando el patrón del diseño.
            val step = 20.dp.toPx()
            val stroke = 10.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(
                    color = CuidaSurfaceMuted,
                    start = Offset(x, 0f),
                    end = Offset(x + size.height, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Square
                )
                x += step
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 4.dp, color = CuidaGreenAccent, shape = CircleShape)
            )
            Text(
                text = "mapa: tu ubicación en tiempo real",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = CuidaTextSecondary
            )
        }
    }
}

// Acciones inferiores: llamar al 112 y cancelar.
@Composable
private fun EmergencyActions(onCall: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .navigationBarsPadding()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CuidaRed)
                .clickable(role = Role.Button, onClick = onCall)
                .padding(vertical = 18.dp)
                .semantics { contentDescription = "SOS. Llamar al $EMERGENCY_PHONE" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS · Llamar al $EMERGENCY_PHONE",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = "Estoy bien, cancelar alerta",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onCancel)
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextSecondary,
            textDecoration = TextDecoration.Underline
        )
    }
}
