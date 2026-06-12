package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle

// Datos de "Continuar donde lo dejaste" y estadísticas: placeholders del
// diseño 2B. La app todavía no rastrea progreso por juego ni minutos jugados.
private const val DAILY_MINUTES_LABEL = "12 minutos al día"
private const val STREAK_DAYS = "6 días"
private const val MINUTES_TODAY = "7 min"

private data class ResumeGame(val name: String, val progress: Float)

private val resumeGames = listOf(
    ResumeGame("Parejas de cartas", 0.70f),
    ResumeGame("Palabras encadenadas", 0.45f),
    ResumeGame("¿Qué falta aquí?", 0.25f)
)

/**
 * Centro de estimulación cognitiva (sección 2B "Tu entrenamiento" del diseño).
 * La tarjeta principal "Juego del día" es el único acceso al juego de los
 * contactos (GameScreen) tras retirarlo de la barra de navegación.
 */
@Composable
fun CognitiveCenterScreen(
    modifier: Modifier = Modifier,
    onPlayGame: () -> Unit = {}
) {
    val today = LocalDate.now()
    val dayName = today.dayOfWeek
        .getDisplayName(TextStyle.FULL, spanishLocale)
        .replaceFirstChar { it.uppercase(spanishLocale) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        Column {
            Text(
                text = "Tu entrenamiento",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "$dayName · $DAILY_MINUTES_LABEL",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
        }

        DailyGameCard(onPlayGame = onPlayGame)

        ResumeGamesSection(onPlayGame = onPlayGame)

        StatsRow()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DailyGameCard(onPlayGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp), spotColor = CuidaTextPrimary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(1.dp, CuidaBorder, RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = CuidaGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "JUEGO DEL DÍA",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = CuidaGreen
            )
            Text(
                text = "Identificar familiar",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "Memoria · 5 minutos",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
            Button(
                onClick = onPlayGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .semantics { contentDescription = "Jugar ahora al juego de identificar familiar" },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CuidaGreen,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Jugar ahora", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ResumeGamesSection(onPlayGame: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Continuar donde lo dejaste",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            resumeGames.forEach { game ->
                ResumeGameCard(
                    game = game,
                    onClick = onPlayGame,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResumeGameCard(
    game: ResumeGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentLabel = "${(game.progress * 100).toInt()}%"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, CuidaBorder, RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "Continuar ${game.name}, progreso $percentLabel"
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { game.progress },
                modifier = Modifier.size(44.dp),
                color = CuidaGreen,
                trackColor = CuidaDivider,
                strokeWidth = 5.dp
            )
            Text(
                text = percentLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
        }
        Text(
            text = game.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            value = STREAK_DAYS,
            label = "de racha seguida",
            container = CuidaGreenSurface,
            valueColor = CuidaGreenDark,
            labelColor = CuidaGreenDark.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = MINUTES_TODAY,
            label = "jugados hoy",
            container = CuidaSurfaceMuted,
            valueColor = CuidaTextPrimary,
            labelColor = CuidaTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    container: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .semantics(mergeDescendants = true) {}
            .padding(14.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
        Text(text = label, fontSize = 12.sp, color = labelColor)
    }
}
