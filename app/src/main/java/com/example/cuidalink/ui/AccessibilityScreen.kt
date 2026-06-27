package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.theme.CuidaBorder
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaSurfaceFaint
import com.example.cuidalink.ui.theme.CuidaSurfaceMuted
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.Urbanist
import com.example.cuidalink.viewmodel.AccessibilityViewModel

// Rango de escala del texto (multiplicador) y tamaño base de la vista previa.
private const val TEXT_SIZE_MIN = 0.8f
private const val TEXT_SIZE_MAX = 1.5f
private const val PREVIEW_BASE_SIZE_SP = 16f

/** Pantalla de Accesibilidad (solo UI): refleja y edita el estado del ViewModel. */
@Composable
fun AccessibilityScreen(
    modifier: Modifier = Modifier,
    viewModel: AccessibilityViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaSurfaceFaint)
    ) {
        AccessibilityTopBar(onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TextSizeCard(
                    multiplier = state.textSizeMultiplier,
                    onMultiplierChange = viewModel::updateTextSize
                )
            }
            item {
                ToggleCard(
                    title = "Alto Contraste",
                    description = "Mejora la legibilidad con colores intensos",
                    checked = state.isHighContrastEnabled,
                    onCheckedChange = viewModel::toggleHighContrast
                )
            }
            item {
                ToggleCard(
                    title = "Reducir Animaciones",
                    description = "Disminuye el movimiento en pantalla",
                    checked = state.reduceAnimations,
                    onCheckedChange = viewModel::toggleAnimations
                )
            }
        }
    }
}

// Cabecera: flecha de retroceso y título centrado, limpio.
@Composable
private fun AccessibilityTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = CuidaTextPrimary
            )
        }
        Text(
            text = "Accesibilidad",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontFamily = Urbanist,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        // Hueco simétrico al botón de volver para que el título quede centrado.
        Spacer(modifier = Modifier.size(48.dp))
    }
}

// Tarjeta 1: ajuste de tamaño de texto con slider y vista previa en vivo.
@Composable
private fun TextSizeCard(multiplier: Float, onMultiplierChange: (Float) -> Unit) {
    AccessibilityCard {
        Text(
            text = "Tamaño del Texto",
            fontFamily = Urbanist,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = multiplier,
            onValueChange = onMultiplierChange,
            valueRange = TEXT_SIZE_MIN..TEXT_SIZE_MAX,
            colors = SliderDefaults.colors(
                thumbColor = CuidaGreen,
                activeTrackColor = CuidaGreen,
                inactiveTrackColor = CuidaGreenSurfaceHover
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CuidaSurfaceMuted)
                .padding(vertical = 18.dp, horizontal = 16.dp)
                .semantics { contentDescription = "Vista previa del tamaño de letra" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Vista previa del tamaño de letra",
                textAlign = TextAlign.Center,
                // El tamano base; la escala del usuario se aplica de forma global.
                fontSize = PREVIEW_BASE_SIZE_SP.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextPrimary
            )
        }
    }
}

// Tarjetas 2 y 3: título + descripción a la izquierda y Switch a la derecha.
@Composable
private fun ToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    AccessibilityCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Urbanist,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = CuidaTextSecondary
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CuidaGreen,
                    checkedBorderColor = CuidaGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = CuidaBorder,
                    uncheckedBorderColor = CuidaBorder
                )
            )
        }
    }
}

// Contenedor blanco con bordes muy redondeados, sombra suave y padding interno.
@Composable
private fun AccessibilityCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}
