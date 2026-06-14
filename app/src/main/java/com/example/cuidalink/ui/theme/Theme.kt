package com.example.cuidalink.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// El diseño de CuidaLink define una única apariencia clara con la paleta verde.
// Se desactiva el color dinámico para respetar la identidad visual del prototipo.
private val CuidaLinkColorScheme = lightColorScheme(
    primary = CuidaGreen,
    onPrimary = Color.White,
    primaryContainer = CuidaGreenSurface,
    onPrimaryContainer = CuidaGreenDark,
    secondary = CuidaGreenDark,
    onSecondary = Color.White,
    secondaryContainer = CuidaGreenSurfaceHover,
    onSecondaryContainer = CuidaGreenDark,
    background = Color.White,
    onBackground = CuidaTextPrimary,
    surface = Color.White,
    onSurface = CuidaTextPrimary,
    surfaceVariant = CuidaSurfaceMuted,
    onSurfaceVariant = CuidaTextSecondary,
    outline = CuidaBorder,
    outlineVariant = CuidaDivider,
    error = CuidaRed,
    onError = Color.White
)

@Composable
fun CuidaLinkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CuidaLinkColorScheme,
        typography = Typography
    ) {
        // Aplica Urbanist por defecto a TODO el texto, incluido el que no
        // usa explícitamente un estilo del tema (Text con solo tamaño/peso).
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = Urbanist),
            content = content
        )
    }
}
