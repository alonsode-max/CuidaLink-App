package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.Urbanist

// Cabecera compacta reutilizable: banda verde con esquinas inferiores
// redondeadas, icono en píldora translúcida y el nombre de la pantalla. Sirve
// para que el paciente reconozca de un vistazo dónde está, sin ocupar tanto
// espacio como la cabecera grande del Home.
//
// Comparte el degradado verde (headerGradient) con el header del Home para que
// ambas cabeceras se vean idénticas.
@Composable
fun ScreenHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Banda verde plana (sin redondeo abajo). El panel de contenido de
            // cada pantalla se solapa hacia arriba con esquinas superiores
            // redondeadas, igual que en el Home.
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF17A34A),Color(0xFF17A34A), Color(0xFFE4E5E4))
                )
            )
            // Padding inferior extra para que el panel pueda solaparse sin tapar
            // el título.
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 40.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = subtitle?.let { "$title. $it" } ?: title
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono blanco sin fondo.
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Urbanist,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            trailing?.invoke()
        }
    }
}
