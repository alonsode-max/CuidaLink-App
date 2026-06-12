package com.example.cuidalink.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaGreenDark
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary

/** Pestaña de la barra de navegación flotante. */
data class FloatingNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Barra de navegación inferior flotante estilo One UI / Material You:
 * píldora despegada de los bordes, con el ítem activo resaltado por un
 * fondo redondeado detrás del icono y el texto.
 *
 * Se construye con una Row propia porque NavigationBar nativo no permite
 * la forma de píldora flotante ni el resaltado por ítem que pide el diseño.
 */
@Composable
fun FloatingNavBar(
    items: List<FloatingNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                FloatingNavBarItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavBarItem(
    item: FloatingNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) CuidaGreenSurface else Color.Transparent,
        label = "navItemBackground"
    )
    val contentColor = if (isSelected) CuidaGreenDark else CuidaTextSecondary

    Box(
        modifier = modifier
            // Objetivo táctil amplio (>=48dp) para accesibilidad de mayores.
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(containerColor)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics {
                selected = isSelected
                contentDescription = "Ir a ${item.label}"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor
            )
            Text(
                text = item.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) CuidaGreenDark else CuidaTextPrimary.copy(alpha = 0.75f)
            )
        }
    }
}
