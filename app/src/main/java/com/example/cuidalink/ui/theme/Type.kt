package com.example.cuidalink.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.cuidalink.R

// Mapea un peso de Compose al eje 'wght' de una fuente variable.
@OptIn(ExperimentalTextApi::class)
private fun variableWeight(resId: Int, axisWeight: Int, fontWeight: FontWeight) = Font(
    resId = resId,
    weight = fontWeight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axisWeight))
)

// Urbanist (fuente variable, OFL) empaquetada en res/font.
val Urbanist = FontFamily(
    variableWeight(R.font.urbanist, 400, FontWeight.Normal),
    variableWeight(R.font.urbanist, 500, FontWeight.Medium),
    variableWeight(R.font.urbanist, 600, FontWeight.SemiBold),
    variableWeight(R.font.urbanist, 700, FontWeight.Bold),
    variableWeight(R.font.urbanist, 800, FontWeight.ExtraBold),
    variableWeight(R.font.urbanist, 900, FontWeight.Black)
)

// Mantenemos las antiguas por compatibilidad mientras se migra el código,
val LexendDeca = Urbanist
val Nunito = Urbanist

// Material 3 Typography configurada con Urbanist.
private val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = Urbanist),
    displayMedium = baseline.displayMedium.copy(fontFamily = Urbanist),
    displaySmall = baseline.displaySmall.copy(fontFamily = Urbanist),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = Urbanist),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = Urbanist),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = Urbanist),
    titleLarge = baseline.titleLarge.copy(fontFamily = Urbanist),
    titleMedium = baseline.titleMedium.copy(fontFamily = Urbanist),
    titleSmall = baseline.titleSmall.copy(fontFamily = Urbanist),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = Urbanist),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Urbanist),
    bodySmall = baseline.bodySmall.copy(fontFamily = Urbanist),
    labelLarge = baseline.labelLarge.copy(fontFamily = Urbanist),
    labelMedium = baseline.labelMedium.copy(fontFamily = Urbanist),
    labelSmall = baseline.labelSmall.copy(fontFamily = Urbanist)
)
