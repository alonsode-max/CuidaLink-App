package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.viewmodel.CaregiverZoneAlertViewModel
import org.osmdroid.util.GeoPoint

/**
 * Pantalla que ve el CUIDADOR cuando su paciente sale de la zona segura: avisa de la
 * salida y muestra su ubicación (actual o la última conocida), con botón a Google Maps.
 */
@Composable
fun CaregiverZoneAlertScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: CaregiverZoneAlertViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val location = if (state.lat != null && state.lng != null) {
        GeoPoint(state.lat!!, state.lng!!)
    } else {
        null
    }
    val name = state.patientName ?: "El paciente"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "ALERTA DE ZONA SEGURA",
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(98.dp)
                    .clip(CircleShape)
                    .background(CuidaRedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = CuidaRed,
                    modifier = Modifier.size(52.dp)
                )
            }

            Text(
                text = "$name salió de la zona segura",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "Esta es su ubicación:",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = CuidaTextSecondary
            )

            if (location != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .semantics { contentDescription = "Mapa con la ubicación del paciente" },
                    contentAlignment = Alignment.Center
                ) {
                    OsmMap(
                        center = location,
                        zoom = 16.0,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(width = 4.dp, color = CuidaRed, shape = CircleShape)
                    )
                }
                OpenInMapsButton(
                    onClick = { openInGoogleMaps(context, state.lat!!, state.lng!!, name) }
                )
            } else {
                Text(
                    text = "Ubicación del paciente no disponible aún.",
                    fontSize = 13.sp,
                    color = CuidaTextSecondary
                )
            }
        }

        Text(
            text = "Cerrar",
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onBack)
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextSecondary,
            textDecoration = TextDecoration.Underline
        )
    }
}
