package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.LocalDarkThemeActive
import com.example.cuidalink.ui.theme.keepOriginalColorsInDark
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

// Teléfono de emergencias. La llamada la confirma la persona usuaria (se abre
const val EMERGENCY_PHONE = "112"

// Geometría de la barra con recorte y del FAB acoplado. El radio del recorte
private val FAB_SIZE = 72.dp
private val NOTCH_RADIUS = 44.dp
private val BAR_HEIGHT = 72.dp
// Radio grande: con clamp a media altura la barra queda como pildora.
private val BAR_CORNER = 100.dp

// Desplazamiento que baja el FAB centrado hasta encajar en el recorte:
val FAB_DOCK_OFFSET = FAB_SIZE / 2 + 16.dp

/** Boton SOS: FAB redondo grande de alto contraste. */
@Composable
fun SosFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        // El SOS conserva su rojo y su texto blanco también en modo oscuro.
        modifier = modifier.size(FAB_SIZE).keepOriginalColorsInDark(),
        contentAlignment = Alignment.Center
    ) {
        // Resplandor rojo difuso que asoma por los bordes del botón (glow). En
        if (!LocalDarkThemeActive.current) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .clip(CircleShape)
                    .background(CuidaRed)
            )
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "SOS, pedir ayuda. Llama al $EMERGENCY_PHONE" },
            shape = CircleShape,
            containerColor = CuidaRed,
            contentColor = Color.White,
            // Sin elevación propia: el relieve lo da el resplandor difuso de atrás.
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            )
        ) {
            // Borde blanco interior (anillo) dentro del círculo rojo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
                    .border(width = 3.dp, color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/** Barra inferior estilo pildora flotante con hueco central para el SOS. */
@Composable
fun SosBottomBar(
    leftItems: List<FloatingNavItem>,
    rightItems: List<FloatingNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = remember {
        CutoutBottomBarShape(cornerRadius = BAR_CORNER, cutoutRadius = NOTCH_RADIUS)
    }
    // En modo oscuro la sombra invertida se compensa.
    val barElevation = if (LocalDarkThemeActive.current) 0.dp else 14.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            // Despega la píldora de los bordes (sin padding arriba: el borde
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .shadow(elevation = barElevation, shape = shape)
            .clip(shape)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftItems.forEach { item ->
                    NavBarButton(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
            // Hueco central: deja libre el ancho del recorte para que ningún
            Spacer(modifier = Modifier.width(FAB_SIZE + 20.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rightItems.forEach { item ->
                    NavBarButton(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

// Acceso de la barra: solo icono dentro de un objetivo táctil amplio. El
@Composable
private fun NavBarButton(
    item: FloatingNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // En modo oscuro los iconos van blancos con contra-filtro.
    val dark = LocalDarkThemeActive.current
    val tint = when {
        dark -> Color.White
        isSelected -> CuidaGreen
        else -> CuidaTextSecondary
    }
    val background = if (isSelected) CuidaGreenSurface else Color.Transparent

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics {
                selected = isSelected
                contentDescription = "Ir a ${item.label}"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(26.dp)
                .keepOriginalColorsInDark()
        )
    }
}

/** Forma de la barra inferior: pildora con bordes muy redondeados. */
private class CutoutBottomBarShape(
    private val cornerRadius: Dp,
    private val cutoutRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val rc = min(with(density) { cornerRadius.toPx() }, size.height / 2f)
        val r = with(density) { cutoutRadius.toPx() }
        val w = size.width
        val h = size.height
        val cx = w / 2f
        // Radio de los hombros redondeados en la entrada del recorte.
        val s = with(density) { 12.dp.toPx() }

        // Cradle: el recorte es un semicírculo de radio r centrado en (cx, 0)
        val dx = sqrt(r * r + 2f * r * s)        // |cx - centro del hombro|
        val flCenterX = cx - dx                  // centro hombro izquierdo (y = s)
        val frCenterX = cx + dx                  // centro hombro derecho
        val tpX = r * dx / (r + s)               // offset en x del punto tangente
        val tpY = r * s / (r + s)                // y del punto tangente
        val tpLeftX = cx - tpX
        val tpRightX = cx + tpX

        fun deg(y: Float, x: Float) = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()

        val leftShoulderStart = -90f
        val leftShoulderEnd = deg(tpY - s, tpLeftX - flCenterX)
        val cutoutStart = deg(tpY, tpLeftX - cx)
        val cutoutEnd = deg(tpY, tpRightX - cx)
        val rightShoulderStart = deg(tpY - s, tpRightX - frCenterX)
        val rightShoulderEnd = -90f

        val path = Path().apply {
            // Esquina superior izquierda.
            moveTo(0f, rc)
            arcTo(Rect(0f, 0f, 2 * rc, 2 * rc), 180f, 90f, false)
            // Borde superior hasta el hombro izquierdo del recorte.
            lineTo(flCenterX, 0f)
            // Hombro izquierdo (convexo, redondea la entrada).
            arcTo(
                Rect(flCenterX - s, 0f, flCenterX + s, 2 * s),
                leftShoulderStart,
                leftShoulderEnd - leftShoulderStart,
                false
            )
            // Recorte semicircular (cóncavo) que envuelve el FAB.
            arcTo(
                Rect(cx - r, -r, cx + r, r),
                cutoutStart,
                cutoutEnd - cutoutStart,
                false
            )
            // Hombro derecho (convexo).
            arcTo(
                Rect(frCenterX - s, 0f, frCenterX + s, 2 * s),
                rightShoulderStart,
                rightShoulderEnd - rightShoulderStart,
                false
            )
            // Borde superior hasta la esquina superior derecha.
            lineTo(w - rc, 0f)
            arcTo(Rect(w - 2 * rc, 0f, w, 2 * rc), 270f, 90f, false)
            // Lado derecho y esquina inferior derecha.
            lineTo(w, h - rc)
            arcTo(Rect(w - 2 * rc, h - 2 * rc, w, h), 0f, 90f, false)
            // Base y esquina inferior izquierda.
            lineTo(rc, h)
            arcTo(Rect(0f, h - 2 * rc, 2 * rc, h), 90f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}
