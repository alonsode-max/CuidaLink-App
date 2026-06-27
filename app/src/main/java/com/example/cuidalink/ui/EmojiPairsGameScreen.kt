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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.*
import kotlinx.coroutines.delay

// Juego "Parejas de Emojis": destapar cartas de a dos para emparejar.

// Emojis del juego. Se eligieron caras de animales amigables, bien diferenciadas
private val EMOJI_DECK = listOf("🐶", "🐱", "🦊", "🐻", "🐰", "🦁")

private const val GRID_COLUMNS = 3
private const val MISMATCH_DELAY_MS = 900L

private data class EmojiCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

// Crea un tablero nuevo: emojis duplicados y barajados con id estable.
private fun createBoard(): List<EmojiCard> =
    (EMOJI_DECK + EMOJI_DECK)
        .shuffled()
        .mapIndexed { index, emoji -> EmojiCard(id = index, emoji = emoji) }

@Composable
fun EmojiPairsGameScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var board by remember { mutableStateOf(createBoard()) }
    var moves by remember { mutableStateOf(0) }

    val flippedUnmatched = board.filter { it.isFlipped && !it.isMatched }
    // Mientras hay dos cartas destapadas se bloquea la entrada hasta resolver.
    val isLocked = flippedUnmatched.size >= 2
    val totalPairs = EMOJI_DECK.size
    val matchedPairs = board.count { it.isMatched } / 2
    val isComplete = matchedPairs == totalPairs

    // Resuelve la pareja cuando hay dos cartas destapadas: si coinciden quedan
    val flippedIds = flippedUnmatched.map { it.id }
    LaunchedEffect(flippedIds) {
        if (flippedUnmatched.size == 2) {
            val (first, second) = flippedUnmatched
            if (first.emoji == second.emoji) {
                board = board.map { if (it.id in flippedIds) it.copy(isMatched = true) else it }
            } else {
                delay(MISMATCH_DELAY_MS)
                board = board.map { if (it.isFlipped && !it.isMatched) it.copy(isFlipped = false) else it }
            }
        }
    }

    fun onCardClick(card: EmojiCard) {
        if (isLocked || card.isFlipped || card.isMatched) return
        if (flippedUnmatched.isEmpty()) moves++
        board = board.map { if (it.id == card.id) it.copy(isFlipped = true) else it }
    }

    fun restart() {
        board = createBoard()
        moves = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaGameBackground)
    ) {
        GameTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                // Aire inferior generoso para que el contenido baje y no quede
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Título a todo el ancho: puede ser grande sin comprimirse.
            Text(
                text = "Parejas de Emojis",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProgressPill(matchedPairs = matchedPairs, totalPairs = totalPairs)

            Spacer(modifier = Modifier.height(16.dp))

            if (isComplete) {
                CompletedContent(moves = moves, onRestart = ::restart)
            } else {
                Text(
                    text = "Encuentra las parejas iguales",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CuidaTextPrimary
                )

                Spacer(modifier = Modifier.height(18.dp))

                EmojiBoard(
                    board = board,
                    onCardClick = ::onCardClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Intentos: $moves",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CuidaTextSecondary
                )
            }
        }
    }
}

// Barra superior: solo el botón de volver. El título va aparte como encabezado
@Composable
private fun GameTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape, spotColor = CuidaTextPrimary.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = "Volver al centro de entrenamiento" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = CuidaTextPrimary
            )
        }
    }
}

@Composable
private fun ProgressPill(matchedPairs: Int, totalPairs: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaGreenSurface)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .semantics {
                contentDescription = "Parejas encontradas: $matchedPairs de $totalPairs"
            }
    ) {
        Text(
            text = "Parejas: $matchedPairs de $totalPairs",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaGreenDark
        )
    }
}

@Composable
private fun EmojiBoard(
    board: List<EmojiCard>,
    onCardClick: (EmojiCard) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        board.chunked(GRID_COLUMNS).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowCards.forEach { card ->
                    EmojiCardView(
                        card = card,
                        onClick = { onCardClick(card) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Rellena la fila si quedara incompleta para mantener el ancho.
                repeat(GRID_COLUMNS - rowCards.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmojiCardView(
    card: EmojiCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRevealed = card.isFlipped || card.isMatched
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(3.dp, shape, spotColor = CuidaTextPrimary.copy(alpha = 0.15f))
            .clip(shape)
            .background(if (isRevealed) CuidaGreenSurface else Color.White)
            .border(
                width = if (card.isMatched) 2.5.dp else 1.5.dp,
                color = if (card.isMatched) CuidaGreen else CuidaBorderLight,
                shape = shape
            )
            .clickable(
                role = Role.Button,
                enabled = !card.isMatched && !card.isFlipped,
                onClick = onClick
            )
            .semantics {
                contentDescription = if (isRevealed) "Carta con ${card.emoji}" else "Carta tapada"
                stateDescription = when {
                    card.isMatched -> "Emparejada"
                    card.isFlipped -> "Destapada"
                    else -> "Tapada"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRevealed) card.emoji else "❓",
            fontSize = 40.sp,
            // El emoji se ve con su color normal también en modo oscuro.
            modifier = Modifier.keepOriginalColorsInDark()
        )
    }
}

@Composable
private fun CompletedContent(moves: Int, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎉", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "¡Muy bien!",
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(CuidaGreenSurface)
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Encontraste todas las parejas en $moves intentos",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = CuidaGreen,
                contentColor = Color.White
            )
        ) {
            Text(text = "Jugar otra vez", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
