package com.example.cuidalink.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.R
import com.example.cuidalink.ui.icons.HugeIcons
import com.example.cuidalink.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle

// Subtítulo del header. La app todavía no rastrea minutos jugados, así que se
private const val DAILY_MINUTES_LABEL = "12 minutos al día"

/** Centro de estimulación cognitiva (sección 2B "Tu entrenamiento" del diseño). */
@Composable
fun CognitiveCenterScreen(
    modifier: Modifier = Modifier,
    onPlayGame: () -> Unit = {},
    onPlayEmojiPairs: () -> Unit = {}
) {
    val today = LocalDate.now()
    val dayName = today.dayOfWeek
        .getDisplayName(TextStyle.FULL, spanishLocale)
        .replaceFirstChar { it.uppercase(spanishLocale) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header compacto: indica al paciente que está en el entrenamiento.
        ScreenHeader(
            title = "Entrenamiento",
            icon = HugeIcons.Brain,
            subtitle = "$dayName · $DAILY_MINUTES_LABEL"
        )

        // Panel de contenido con esquinas superiores redondeadas que se solapa
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                // Margen inferior para que las tarjetas solapen la cabecera.
                .padding(top = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DailyGameCard(onPlayGame = onPlayGame)

            EmojiPairsCard(onPlay = onPlayEmojiPairs)
        }
    }
}

@Composable
private fun DailyGameCard(onPlayGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            // Ilustracion de familiares, recortada centrada.
            Image(
                painter = painterResource(id = R.drawable.identificar_familiar),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().keepOriginalColorsInDark(),
                contentScale = ContentScale.Crop
            )
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

/** Segundo juego local: "Parejas de Emojis". */
@Composable
private fun EmojiPairsCard(onPlay: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(CuidaGreenSurface),
            contentAlignment = Alignment.Center
        ) {
            // Ilustracion del juego de parejas, recortada centrada.
            Image(
                painter = painterResource(id = R.drawable.parejas_emojis),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().keepOriginalColorsInDark(),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "JUEGO RÁPIDO",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = CuidaGreen
            )
            Text(
                text = "Parejas de Emojis",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )
            Text(
                text = "Memoria · Juego rápido",
                fontSize = 13.sp,
                color = CuidaTextSecondary
            )
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .semantics { contentDescription = "Jugar ahora a Parejas de Emojis" },
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
