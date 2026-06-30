package com.example.cuidalink.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.ui.PatientProfileUi
import com.example.cuidalink.ui.components.OsmMapView
import com.example.cuidalink.ui.components.ProfileErrorView
import com.example.cuidalink.ui.components.ShimmerBox
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.PatientProfileViewModel
import com.example.cuidalink.viewmodel.ProfileUiState
import org.osmdroid.util.GeoPoint

private const val FALLBACK = "—"

/** Ficha detallada del paciente para el cuidador, alimentada desde el backend. */
@Composable
fun CaregiverPatientProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: PatientProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CaregiverTopBar(title = "Perfil del paciente", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 18.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val current = state) {
                is ProfileUiState.Loading -> PatientProfileLoading()
                is ProfileUiState.Error -> ProfileErrorView(
                    message = current.message,
                    onRetry = { viewModel.loadCurrentPatient() }
                )
                is ProfileUiState.Success -> PatientProfileContent(current.data, viewModel)
            }
        }
    }
}

@Composable
private fun PatientProfileContent(data: PatientProfileUi, viewModel: PatientProfileViewModel) {
    var isSettingGeofence by remember { mutableStateOf(false) }
    var selectedGeofencePos by remember { 
        mutableStateOf<GeoPoint?>(
            if (data.geofenceLat != null && data.geofenceLng != null) 
                GeoPoint(data.geofenceLat, data.geofenceLng) 
            else null
        ) 
    }
    var geofenceRadius by remember { mutableFloatStateOf(data.geofenceRadius ?: 100f) }

    IdentityHeader(
        name = data.name,
        condition = "Paciente",
        initials = initialsOf(data.name)
    )

    InfoCard(title = "Ubicación en tiempo real") {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CuidaBorderLight, RoundedCornerShape(16.dp))
        ) {
            val patientLoc = data.patientLat?.let { lat -> data.patientLng?.let { lng -> GeoPoint(lat, lng) } }
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                patientLocation = patientLoc,
                geofenceLocation = selectedGeofencePos,
                geofenceRadius = geofenceRadius,
                onMapClick = { if (isSettingGeofence) selectedGeofencePos = it }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        if (isSettingGeofence) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Radio de la zona: ${geofenceRadius.toInt()} metros",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CuidaTextPrimary
                )
                Slider(
                    value = geofenceRadius,
                    onValueChange = { geofenceRadius = it },
                    valueRange = 50f..1000f,
                    steps = 19
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            selectedGeofencePos?.let {
                                viewModel.updateGeofence(data.uid, it.latitude, it.longitude, geofenceRadius)
                                isSettingGeofence = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen)
                    ) {
                        Text("Guardar Zona", color = Color.White)
                    }
                    Button(
                        onClick = { isSettingGeofence = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancelar", color = Color.Black)
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { isSettingGeofence = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CuidaGreenSurfaceHover),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CuidaGreenDark)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Zona de seguridad", color = CuidaGreenDark, fontSize = 13.sp)
                }
                
                Button(
                    onClick = { viewModel.requestLocation(data.uid) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Solicitar ubicación", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }

    InfoCard(title = "Datos personales") {
        InfoRow(label = "Edad", value = data.age?.let { "$it años" } ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Tipo de sangre", value = data.bloodGroup ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Correo", value = data.email ?: FALLBACK)
    }

    InfoCard(title = "Información Vital") {
        InfoRow(label = "Alergias", value = data.allergies ?: "Sin alergias registradas")
        RowDivider()
        InfoRow(label = "Peso", value = data.weightKg?.let { "$it kg" } ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Altura", value = data.heightM?.let { "$it m" } ?: FALLBACK)
    }
}

@Composable
private fun PatientProfileLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.size(96.dp), shape = RoundedCornerShape(24.dp))
        ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 24.dp))
        ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 16.dp))
    }
    Spacer(modifier = Modifier.height(4.dp))
    repeat(2) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun IdentityHeader(name: String, condition: String, initials: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val avatarShape = RoundedCornerShape(24.dp)
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(avatarShape)
                .background(CuidaGreenSurface)
                .border(4.dp, CuidaGreenSurfaceHover, avatarShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(
            text = condition,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(CuidaGreenSurface)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaGreenDark
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text(
            text = title,
            fontFamily = Urbanist,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            color = CuidaTextSecondary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CuidaBorderLight)
    )
}

/** Iniciales (hasta 2) a partir del nombre, para el avatar. */
private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
