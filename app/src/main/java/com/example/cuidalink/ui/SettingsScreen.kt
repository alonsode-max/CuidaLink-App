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
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.theme.*
import com.example.cuidalink.viewmodel.AccountViewModel
import com.example.cuidalink.viewmodel.LinkViewModel
import com.example.cuidalink.viewmodel.UnlinkState

// Pantalla de Ajustes (accesible desde la pestaña "Ajustes" de la barra, antes

/** Pantalla de Configuracion: perfil, accesibilidad, tema y sesion. */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onOpenTheme: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEdit: () -> Unit = {},
    onUnlinked: () -> Unit = {},
    linkViewModel: LinkViewModel = viewModel(),
    accountViewModel: AccountViewModel = viewModel()
) {
    val unlinkState by linkViewModel.unlinkState.collectAsState()
    val account by accountViewModel.account.collectAsState()
    var showUnlinkConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Avisa del resultado de la desvinculación y limpia el estado para no repetirlo.
    LaunchedEffect(unlinkState) {
        when (val current = unlinkState) {
            is UnlinkState.Success -> {
                Toast.makeText(context, "Vínculo eliminado", Toast.LENGTH_SHORT).show()
                linkViewModel.resetUnlink()
                onUnlinked()
            }
            is UnlinkState.Error -> {
                Toast.makeText(context, current.message, Toast.LENGTH_LONG).show()
                linkViewModel.resetUnlink()
            }
            else -> Unit
        }
    }

    if (showUnlinkConfirm) {
        UnlinkConfirmDialog(
            isLoading = unlinkState is UnlinkState.Loading,
            onConfirm = {
                linkViewModel.unlink()
                showUnlinkConfirm = false
            },
            onDismiss = { showUnlinkConfirm = false }
        )
    }

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
            ProfileIdentityHeader(
                name = account?.name ?: "…",
                initials = account?.initials ?: ""
            )

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
            SettingsRow(
                icon = Icons.Filled.Info,
                label = "Centro de ayuda",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://landing-page-cuida-link.vercel.app/contacto"))
                    context.startActivity(intent)
                }
            )

            SettingsSectionTitle(title = "Vinculación")
            SettingsRow(
                icon = Icons.Filled.LinkOff,
                label = "Desvincular",
                iconTint = CuidaRed,
                labelColor = CuidaRed,
                onClick = { showUnlinkConfirm = true }
            )

            SettingsSectionTitle(title = "Sesión")
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Cerrar sesión",
                onClick = onLogout
            )
        }
    }
}

// Diálogo de confirmación antes de romper el vínculo paciente ↔ cuidador.
@Composable
private fun UnlinkConfirmDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Desvincular", fontWeight = FontWeight.ExtraBold, color = CuidaTextPrimary)
        },
        text = {
            Text(
                "¿Seguro que quieres romper el vínculo? Dejaréis de estar conectados y " +
                    "tendréis que volver a vincularos con el código.",
                color = CuidaTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                Text(
                    text = if (isLoading) "Desvinculando…" else "Desvincular",
                    color = CuidaRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CuidaTextSecondary)
            }
        },
        containerColor = Color.White
    )
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
private fun ProfileIdentityHeader(name: String, initials: String) {
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
    iconTint: Color = CuidaGreen,
    labelColor: Color = CuidaTextPrimary,
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
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CuidaTextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
