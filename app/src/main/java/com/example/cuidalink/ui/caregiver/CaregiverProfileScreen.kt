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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.model.ui.CaregiverProfileUi
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
import com.example.cuidalink.viewmodel.CaregiverProfileViewModel
import com.example.cuidalink.viewmodel.ProfileUiState

private const val FALLBACK = "—"

/** Ficha del cuidador (su propio perfil) alimentada desde el backend. */
@Composable
fun CaregiverProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenLinking: () -> Unit = {},
    viewModel: CaregiverProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CaregiverTopBar(title = "Mi perfil", onBack = onBack)

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
                is ProfileUiState.Loading -> CaregiverProfileLoading()
                is ProfileUiState.Error -> ProfileErrorView(
                    message = current.message,
                    onRetry = { viewModel.loadCurrentCaregiver() }
                )
                is ProfileUiState.Success -> CaregiverProfileContent(
                    data = current.data,
                    onOpenLinking = onOpenLinking
                )
            }
        }
    }
}

@Composable
private fun CaregiverProfileContent(data: CaregiverProfileUi, onOpenLinking: () -> Unit) {
    IdentityHeader(
        name = data.name,
        subtitle = "Cuidador",
        initials = initialsOf(data.name)
    )

    InfoCard(title = "Datos personales") {
        InfoRow(label = "Correo", value = data.email ?: FALLBACK)
    }

    InfoCard(title = "Paciente a cargo") {
        if (data.isLinked) {
            InfoRow(label = "Nombre", value = data.patientName ?: FALLBACK)
            RowDivider()
            InfoRow(label = "Correo", value = data.patientEmail ?: FALLBACK)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No tienes un paciente vinculado. Por favor, vincula uno.",
                    fontSize = 15.sp,
                    color = CuidaTextSecondary,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onOpenLinking,
                    colors = ButtonDefaults.buttonColors(containerColor = CuidaGreen),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Vincular ahora")
                }
            }
        }
    }
}

@Composable
private fun CaregiverProfileLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.size(96.dp), shape = RoundedCornerShape(24.dp))
        ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 24.dp))
        ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 16.dp))
    }
    Spacer(modifier = Modifier.height(4.dp))
    repeat(2) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun IdentityHeader(name: String, subtitle: String, initials: String) {
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
            text = subtitle,
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
