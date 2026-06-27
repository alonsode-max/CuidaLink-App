package com.example.cuidalink.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.CuidaBorderLight
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaRedSurface
import com.example.cuidalink.ui.theme.CuidaSurfaceFaint
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist

// Datos médicos de ejemplo (SOLO UI). Estos valores fijos se sustituyen cuando
private const val PROFILE_NAME = "Ernesto García"
private const val PROFILE_CODE = "@E1234"
private const val PROFILE_AGE = "78"
private const val PROFILE_BLOOD_TYPE = "O+"
private const val PROFILE_ALLERGY = "Alergia a la Penicilina"
private const val PROFILE_WEIGHT = "75 kg"
private const val PROFILE_HEIGHT = "1.70 m"
private const val DOCTOR_NAME = "Dra. Martínez"
private const val DOCTOR_PHONE = "600123456"

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenContacts: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaSurfaceFaint)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileTopBar(onBack = onBack)
        ProfileIdentity()
        StatsCardWithPhoto()
        VitalInfoCard()
        SupportNetworkCard()
        ContactsButton(onOpenContacts = onOpenContacts)
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
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver a inicio",
                tint = CuidaTextPrimary
            )
        }
    }
}

// 1. Cabecera de texto: nombre grande y código de vinculación, centrados.
@Composable
private fun ProfileIdentity() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = PROFILE_NAME,
            fontFamily = Urbanist,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Text(
            text = PROFILE_CODE,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextSecondary
        )
    }
}

// 2. Tarjeta central: estadísticas (edad y grupo sanguíneo).
private val PHOTO_SIZE = 108.dp
private val PHOTO_OVERHANG = 50.dp

@Composable
private fun StatsCardWithPhoto() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Tarjeta blanca con sombra ligera. Deja hueco arriba para la foto.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PHOTO_OVERHANG)
                .shadow(4.dp, RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(top = PHOTO_SIZE - PHOTO_OVERHANG + 20.dp, bottom = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                value = PROFILE_AGE,
                label = "Años",
                valueColor = CuidaGreen,
                description = "Edad: $PROFILE_AGE años",
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(1.dp)
                    .background(CuidaBorderLight)
            )
            StatColumn(
                value = PROFILE_BLOOD_TYPE,
                label = "Grupo Sanguíneo",
                valueColor = CuidaTextPrimary,
                description = "Grupo sanguíneo: $PROFILE_BLOOD_TYPE",
                modifier = Modifier.weight(1f)
            )
        }

        // Foto de perfil superpuesta (con botón de edición), con anillo blanco
        ProfilePhotoWithEdit()
    }
}

// Foto de perfil con un botón de edición que abre un menú con dos opciones:
@Composable
private fun ProfilePhotoWithEdit() {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(PHOTO_SIZE)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(CuidaGreenSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HugeIcons.User,
                    contentDescription = "Foto de perfil de $PROFILE_NAME",
                    tint = CuidaGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Botón de edición (lápiz) en la esquina inferior de la foto.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(CuidaGreen)
                .border(2.dp, Color.White, CircleShape)
                .clickable { showMenu = true }
                .semantics { contentDescription = "Editar perfil" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = Color.White
        ) {
            DropdownMenuItem(
                text = { Text("Editar foto de perfil", fontWeight = FontWeight.SemiBold) },
                onClick = { showMenu = false /* TODO: conectar selección de foto */ },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = CuidaGreen)
                }
            )
            DropdownMenuItem(
                text = { Text("Editar información del perfil", fontWeight = FontWeight.SemiBold) },
                onClick = { showMenu = false /* TODO: conectar edición de datos */ },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = CuidaGreen)
                }
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    valueColor: Color,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            fontFamily = Urbanist,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = CuidaTextSecondary
        )
    }
}

// 3a. Tarjeta "Información Vital" (estilo bento): alergias, peso y altura.
@Composable
private fun VitalInfoCard() {
    BentoCard(title = "Información Vital") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CuidaRedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HugeIcons.Heart,
                    contentDescription = null,
                    tint = CuidaRed,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = PROFILE_ALLERGY,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CuidaRed
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasureItem(label = "Peso", value = PROFILE_WEIGHT, modifier = Modifier.weight(1f))
            MeasureItem(label = "Altura", value = PROFILE_HEIGHT, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MeasureItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = CuidaTextSecondary)
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
    }
}

// 3b. Tarjeta "Red de Apoyo": médico de referencia y su teléfono.
@Composable
private fun SupportNetworkCard() {
    val context = LocalContext.current

    BentoCard(title = "Red de Apoyo") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CuidaGreenSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HugeIcons.Stethoscope,
                    contentDescription = null,
                    tint = CuidaGreenDark,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DOCTOR_NAME,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
                Text(
                    text = DOCTOR_PHONE,
                    fontSize = 14.sp,
                    color = CuidaTextSecondary
                )
            }
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$DOCTOR_PHONE"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CuidaGreen)
                    .semantics { contentDescription = "Llamar a $DOCTOR_NAME" }
            ) {
                Icon(
                    imageVector = HugeIcons.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// Botón principal "Ver mis contactos": abre la pantalla de contactos. Reutiliza
@Composable
private fun ContactsButton(onOpenContacts: () -> Unit) {
    Button(
        onClick = onOpenContacts,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Ver mis contactos" },
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = CuidaGreen,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = HugeIcons.User,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Ver mis contactos",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// Contenedor reutilizable de tarjeta blanca con título (estilo bento box).
@Composable
private fun BentoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
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
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}
