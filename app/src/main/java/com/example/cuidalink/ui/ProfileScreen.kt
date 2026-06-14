package com.example.cuidalink.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaAmberDeep
import com.example.cuidalink.ui.theme.CuidaAmberLabel
import com.example.cuidalink.ui.theme.CuidaAmberSurface
import com.example.cuidalink.ui.theme.CuidaBorder
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaRedDark
import com.example.cuidalink.ui.theme.CuidaRedDeep
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary

// Datos médicos de ejemplo (diseño 4A). Sustituir cuando exista un
// ViewModel de perfil con datos reales del backend.
private const val PROFILE_NAME = "Carmen Delgado"
private const val PROFILE_INITIALS = "CD"
private const val PROFILE_SUBTITLE = "74 años · Madrid"
private const val EMERGENCY_CONTACT_NAME = "Lucía Delgado"
private const val EMERGENCY_CONTACT_RELATION = "Contacto de emergencia · hija"
private const val EMERGENCY_CONTACT_PHONE = "600000000"

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenContacts: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ProfileTopBar(onBack = onBack)
        ProfileIdentity()
        TonalInfoGrid()
        ContactsButton(onOpenContacts = onOpenContacts)
        EmergencyContactCard()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver a inicio",
                tint = CuidaTextPrimary
            )
        }
        Text(
            text = "Perfil",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        // Hueco simétrico al botón de volver para centrar el título.
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ProfileIdentity() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(CuidaGreenSurface)
                .border(4.dp, CuidaGreenSurfaceHover, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = PROFILE_INITIALS,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
        Text(
            text = PROFILE_NAME,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(text = PROFILE_SUBTITLE, fontSize = 14.sp, color = CuidaTextSecondary)
    }
}

@Composable
private fun TonalInfoGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TonalCard(
                label = "Tipo de sangre",
                value = "O+",
                container = CuidaRedSurface,
                labelColor = CuidaRedDark,
                valueColor = CuidaRedDeep,
                description = "Tipo de sangre: O positivo",
                modifier = Modifier.weight(1f)
            )
            TonalCard(
                label = "Alergias",
                value = "Penicilina",
                container = CuidaAmberSurface,
                labelColor = CuidaAmberLabel,
                valueColor = CuidaAmberDeep,
                description = "Alergias: Penicilina",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TonalCard(
                label = "Condición",
                value = "Diabetes tipo 2",
                container = CuidaGreenSurface,
                labelColor = CuidaGreen,
                valueColor = CuidaGreenDark,
                description = "Condición médica: Diabetes tipo 2",
                modifier = Modifier.weight(1f)
            )
            TonalCard(
                label = "Médico",
                value = "Dr. Ibáñez",
                container = CuidaSurfaceMuted,
                labelColor = CuidaTextSecondary,
                valueColor = CuidaTextPrimary,
                description = "Médico de cabecera: Doctor Ibáñez",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TonalCard(
    label: String,
    value: String,
    container: Color,
    labelColor: Color,
    valueColor: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .padding(14.dp)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label.uppercase(spanishLocale),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
private fun ContactsButton(onOpenContacts: () -> Unit) {
    Button(
        onClick = onOpenContacts,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .semantics { contentDescription = "Abrir mis contactos y sus fotos" },
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = CuidaGreen,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Filled.People,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(text = "Contactos", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

// El gráfico semanal de actividad se migró a DashboardScreen (Home).

@Composable
private fun EmergencyContactCard() {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CuidaBorder, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LD",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = EMERGENCY_CONTACT_NAME,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = EMERGENCY_CONTACT_RELATION,
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
        }
        Button(
            onClick = {
                // Abre el marcador con el número; la llamada la confirma la persona usuaria.
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$EMERGENCY_CONTACT_PHONE"))
                context.startActivity(intent)
            },
            modifier = Modifier
                .height(48.dp)
                .semantics { contentDescription = "Llamar a $EMERGENCY_CONTACT_NAME" },
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = CuidaGreen,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = "Llamar", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
