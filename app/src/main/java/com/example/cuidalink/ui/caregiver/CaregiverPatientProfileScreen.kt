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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist

// Ficha del paciente (SOLO UI / simulada) que abre el boton "Ver Perfil".
private const val PATIENT_NAME = "Maria Garcia"
private const val PATIENT_INITIALS = "MG"
private const val PATIENT_AGE = "72 años"
private const val PATIENT_CONDITION = "Alzheimer · Etapa moderada"

/** Ficha detallada del paciente para el cuidador. */
@Composable
fun CaregiverPatientProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
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
            IdentityHeader()

            InfoCard(title = "Datos personales") {
                InfoRow(label = "Edad", value = PATIENT_AGE)
                RowDivider()
                InfoRow(label = "Tipo de sangre", value = "0+")
                RowDivider()
                InfoRow(label = "Dirección", value = "C/ Mayor 12, Badalona")
            }

            InfoCard(title = "Condición médica") {
                InfoRow(label = "Diagnóstico", value = "Alzheimer")
                RowDivider()
                InfoRow(label = "Etapa", value = "Moderada")
                RowDivider()
                InfoRow(label = "Alergias", value = "Penicilina")
                RowDivider()
                InfoRow(label = "Medicación", value = "Donepezilo 10mg")
            }

            InfoCard(title = "Contacto de emergencia") {
                InfoRow(label = "Nombre", value = "Carmen Delgado")
                RowDivider()
                InfoRow(label = "Parentesco", value = "Hija")
                RowDivider()
                InfoRow(label = "Teléfono", value = "+34 600 123 456")
            }
        }
    }
}

@Composable
private fun IdentityHeader() {
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
                text = PATIENT_INITIALS,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
        Text(
            text = PATIENT_NAME,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(
            text = PATIENT_CONDITION,
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
