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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                is ProfileUiState.Success -> PatientProfileContent(current.data)
            }
        }
    }
}

@Composable
private fun PatientProfileContent(data: PatientProfileUi) {
    IdentityHeader(
        name = data.name,
        condition = conditionLabel(data),
        initials = initialsOf(data.name)
    )

    InfoCard(title = "Datos personales") {
        InfoRow(label = "Edad", value = data.age?.let { "$it años" } ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Tipo de sangre", value = data.bloodGroup ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Dirección", value = data.address ?: FALLBACK)
    }

    InfoCard(title = "Condición médica") {
        InfoRow(label = "Diagnóstico", value = data.diagnosis ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Etapa", value = data.stage ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Alergias", value = data.allergies ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Medicación", value = data.medication ?: FALLBACK)
    }

    InfoCard(title = "Contacto de emergencia") {
        InfoRow(label = "Nombre", value = data.emergencyContactName ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Parentesco", value = data.emergencyContactRelation ?: FALLBACK)
        RowDivider()
        InfoRow(label = "Teléfono", value = data.emergencyContactPhone ?: FALLBACK)
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
    repeat(3) {
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

/** Construye el subtítulo de condición (diagnóstico · etapa) con fallback. */
private fun conditionLabel(data: PatientProfileUi): String {
    val parts = listOfNotNull(
        data.diagnosis,
        data.stage?.let { "Etapa $it" }
    )
    return if (parts.isEmpty()) "Paciente" else parts.joinToString(" · ")
}

/** Iniciales (hasta 2) a partir del nombre, para el avatar. */
private fun initialsOf(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
