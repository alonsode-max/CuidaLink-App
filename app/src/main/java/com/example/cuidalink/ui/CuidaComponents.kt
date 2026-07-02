package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaHeaderGradientEnd

// Primitivas de diseño compartidas por las pantallas de CuidaLink. Centralizan

// Dimensiones de diseño. Un único radio de redondeo para todas las tarjetas y
object CuidaDimens {
    val CardRadius: Dp = 24.dp
    val CardElevation: Dp = 3.dp
}

// Degradado verde de las cabeceras: verde sólido que desvanece a un gris muy
val headerGradient: Brush = Brush.verticalGradient(
    listOf(CuidaGreen, CuidaGreen, CuidaGreen, CuidaHeaderGradientEnd)
)

// Estilo base de tarjeta: blanca, redondeada y con sombra tonal suave. Mismo
fun Modifier.cuidaCard(
    radius: Dp = CuidaDimens.CardRadius,
    elevation: Dp = CuidaDimens.CardElevation,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .shadow(elevation = elevation, shape = shape)
        .clip(shape)
        .background(Color.White)
}

// Panel de contenido que se solapa hacia arriba sobre la cabecera verde: ancho
fun Modifier.overlapPanel(radius: Dp = CuidaDimens.CardRadius): Modifier = this
    .fillMaxWidth()
    .clip(RoundedCornerShape(topStart = radius, topEnd = radius))
    .background(Color.White)
