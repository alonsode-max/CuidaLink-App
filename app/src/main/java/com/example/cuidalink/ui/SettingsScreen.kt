package com.example.cuidalink.ui

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.*

// Pantalla de Ajustes (accesible desde la pestaña "Ajustes" de la barra, antes

private const val SETTINGS_NAME = "Carmen Delgado"
private const val SETTINGS_INITIALS = "CD"

/** Pantalla de Configuracion: perfil, accesibilidad, tema y sesion. */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onOpenTheme: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SettingsHeader(onBack = onBack, onEdit = onEdit)

        // Panel claro que se solapa hacia arriba sobre la cabecera navy (mismo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                // Margen inferior amplio para que la barra flotante de navegación
                .padding(top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileIdentityHeader()

            SettingsSectionTitle(title = "Cuenta")
            SettingsRow(
                icon = Icons.Filled.Person,
                label = "Información personal",
                onClick = onOpenProfile
            )

            SettingsSectionTitle(title = "Ajustes")
            SettingsRow(
                icon = Icons.Filled.Accessibility,
                label = "Accesibilidad",
                onClick = onOpenAccessibility
            )
            SettingsRow(icon = Icons.Filled.Language, label = "Idioma")
            SettingsRow(
                icon = Icons.Filled.DarkMode,
                label = "Tema",
                onClick = onOpenTheme
            )

            SettingsSectionTitle(title = "Soporte")
            SettingsRow(icon = Icons.Filled.Info, label = "Centro de ayuda")

            SettingsSectionTitle(title = "Sesión")
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Cerrar sesión",
                onClick = onLogout
            )
        }
    }
}

// Cabecera verde de marca: botón de volver, título "Perfil" centrado y lápiz
@Composable
private fun SettingsHeader(onBack: () -> Unit, onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            // Conserva el verde y el texto blanco también en modo oscuro.
            .keepOriginalColorsInDark()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF17A34A),
                        Color(0xFF17A34A),
                        // En oscuro funde hacia gris oscuro.
                        if (LocalDarkThemeActive.current) Color(0xFF262729) else Color(0xFFE4E5E4)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Más aire arriba para que el título no quede pegado al borde.
                .padding(start = 22.dp, end = 18.dp, top = 38.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuración",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold
            )

        }
    }
}

@Composable
private fun HeaderCircleButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Avatar (iniciales), nombre y correo centrados, bajo la cabecera.
@Composable
private fun ProfileIdentityHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cuadrada con esquinas redondeadas y borde, igual que el avatar del Home.
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
                text = SETTINGS_INITIALS,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreen
            )
        }
        Text(
            text = SETTINGS_NAME,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = CuidaTextPrimary
    )
}

// Fila de ajuste: chip con icono, etiqueta y flecha; onClick opcional.
@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(CuidaSurfaceMuted)
        .then(
            if (onClick != null) {
                Modifier.clickable(role = Role.Button, onClick = onClick)
            } else {
                Modifier
            }
        )
        .padding(horizontal = 14.dp, vertical = 14.dp)
        .semantics { contentDescription = label }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CuidaGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextPrimary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CuidaTextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
