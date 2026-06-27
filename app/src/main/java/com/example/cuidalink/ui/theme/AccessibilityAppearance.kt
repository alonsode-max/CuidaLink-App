package com.example.cuidalink.ui.theme

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect as AndroidRenderEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

// Cuánto se intensifica el contraste en modo "Alto contraste". 1.0 = sin
private const val HIGH_CONTRAST_FACTOR = 1.6f

// Pendiente y desplazamiento de la inversión del modo oscuro. El blanco (255)
private const val DARK_SLOPE = -0.85f
private const val DARK_OFFSET = 255f

private val INVERT_MATRIX = floatArrayOf(
    DARK_SLOPE, 0f, 0f, 0f, DARK_OFFSET,
    0f, DARK_SLOPE, 0f, 0f, DARK_OFFSET,
    0f, 0f, DARK_SLOPE, 0f, DARK_OFFSET,
    0f, 0f, 0f, 1f, 0f
)

// Rotación de matiz de 180° (filas suman 1 => conserva la luminancia). Junto
private val HUE_ROTATE_180_MATRIX = floatArrayOf(
    -0.574f, 1.430f, 0.144f, 0f, 0f,
    0.426f, 0.430f, 0.144f, 0f, 0f,
    0.426f, 1.430f, -0.856f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
)

// Indica a los composables si el modo oscuro está activo.
val LocalDarkThemeActive = staticCompositionLocalOf { false }

/** Envuelve la app y aplica las preferencias globales de apariencia. */
@Composable
fun AccessibilityAppearance(
    darkTheme: Boolean,
    highContrast: Boolean,
    textScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val effect = remember(darkTheme, highContrast) {
        buildAppearanceRenderEffect(darkTheme, highContrast)
    }

    val appearanceModifier = if (effect != null) {
        Modifier.graphicsLayer { renderEffect = effect }
    } else {
        Modifier
    }

    // Escala global del texto: se aplica al `fontScale` de la densidad, de modo
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, textScale) {
        Density(density = baseDensity.density, fontScale = baseDensity.fontScale * textScale)
    }

    CompositionLocalProvider(
        LocalDarkThemeActive provides darkTheme,
        LocalDensity provides scaledDensity
    ) {
        Box(modifier = Modifier.fillMaxSize().then(appearanceModifier)) {
            content()
        }
    }
}

/** Mantiene los colores originales de un elemento aunque cambie el tema. */
@Composable
fun Modifier.keepOriginalColorsInDark(): Modifier {
    val dark = LocalDarkThemeActive.current
    if (!dark) return this
    val counter = remember { buildCounterDarkRenderEffect() }
    return this.graphicsLayer { renderEffect = counter }
}

// Compone las matrices según las opciones activas (null = apariencia normal).
private fun buildAppearanceRenderEffect(darkTheme: Boolean, highContrast: Boolean): RenderEffect? {
    if (!darkTheme && !highContrast) return null

    val matrix = ColorMatrix()
    if (darkTheme) {
        matrix.postConcat(ColorMatrix(INVERT_MATRIX))
        matrix.postConcat(ColorMatrix(HUE_ROTATE_180_MATRIX))
    }
    if (highContrast) {
        val factor = HIGH_CONTRAST_FACTOR
        val bias = (0.5f - 0.5f * factor) * 255f
        matrix.postConcat(
            ColorMatrix(
                floatArrayOf(
                    factor, 0f, 0f, 0f, bias,
                    0f, factor, 0f, 0f, bias,
                    0f, 0f, factor, 0f, bias,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    }
    return AndroidRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(matrix)).asComposeRenderEffect()
}

// Inverso exacto del filtro oscuro (invert + hue). Como la rotación de 180° es
private fun buildCounterDarkRenderEffect(): RenderEffect {
    val invFactor = 1f / DARK_SLOPE          // recíproco de la pendiente
    val invOffset = -DARK_OFFSET / DARK_SLOPE // desplazamiento que la cancela
    val matrix = ColorMatrix()
    matrix.postConcat(ColorMatrix(HUE_ROTATE_180_MATRIX))
    matrix.postConcat(
        ColorMatrix(
            floatArrayOf(
                invFactor, 0f, 0f, 0f, invOffset,
                0f, invFactor, 0f, 0f, invOffset,
                0f, 0f, invFactor, 0f, invOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
    return AndroidRenderEffect.createColorFilterEffect(ColorMatrixColorFilter(matrix)).asComposeRenderEffect()
}
