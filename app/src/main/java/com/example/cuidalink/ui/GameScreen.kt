package com.example.cuidalink.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cuidalink.ui.theme.*
import com.example.cuidalink.viewmodel.GameViewModel

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.gameState.collectAsState()

    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
        if (isGranted) {
            viewModel.fetchContacts(context.contentResolver)
        }
    }

    LaunchedEffect(hasContactPermission) {
        if (hasContactPermission) {
            viewModel.fetchContacts(context.contentResolver)
        } else {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CuidaGameBackground)
    ) {
        GameTopBar(onBack = onBack)
        when {
            !hasContactPermission -> PermissionRequest(onRequest = { launcher.launch(Manifest.permission.READ_CONTACTS) })
            state.isLoading -> LoadingIndicator()
            state.isGameOver -> GameOverContent(
                score = state.score,
                message = state.message,
                onRestart = { viewModel.fetchContacts(context.contentResolver) }
            )
            else -> state.currentContact?.let { contact ->
                GameQuestionContent(
                    contactKey = contact.id,
                    photoUri = contact.photoUri,
                    options = state.options,
                    questionNumber = state.currentQuestionNumber,
                    totalQuestions = state.totalQuestions,
                    score = state.score,
                    feedbackMessage = state.message,
                    onConfirm = { guess -> viewModel.checkGuess(guess) }
                )
            }
        }
    }
}

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
        Text(
            text = "Identificar familiar",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        // Hueco simétrico para que el título quede centrado.
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Se necesita permiso para leer contactos",
            fontSize = 16.sp,
            color = CuidaTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryActionButton(text = "Conceder permiso", onClick = onRequest)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CuidaGreen)
    }
}

@Composable
private fun GameOverContent(score: Int, message: String?, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Juego terminado!",
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
                text = "Puntuación final: $score",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaGreenDark
            )
        }
        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, fontSize = 14.sp, color = CuidaTextSecondary, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryActionButton(text = "Jugar otra vez", onClick = onRestart)
    }
}

@Composable
private fun GameQuestionContent(
    contactKey: String,
    photoUri: android.net.Uri?,
    options: List<String>,
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
    feedbackMessage: String?,
    onConfirm: (String) -> Unit
) {
    // La opción elegida es estado de UI: se confirma con el botón inferior,
    // siguiendo el flujo "seleccionar → Confirmar respuesta" del diseño.
    var selectedOption by remember(contactKey) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        GameProgressBar(questionNumber = questionNumber, totalQuestions = totalQuestions)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "¿Quién es esta persona?",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 30.sp,
            color = CuidaTextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = CuidaTextPrimary.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Foto del familiar a identificar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CuidaGreenSurface),
                contentScale = ContentScale.Crop
            )
        }

        feedbackMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            val isCorrect = it.startsWith("¡Correcto!")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isCorrect) CuidaGreenSurface else Color(0xFFFCEAE8))
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) CuidaGreenDark else CuidaRedDark
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                AnswerOptionCard(
                    text = option,
                    isSelected = option == selectedOption,
                    onClick = { selectedOption = option }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Puntuación: $score",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CuidaTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryActionButton(
            text = "Confirmar respuesta",
            enabled = selectedOption != null,
            onClick = { selectedOption?.let(onConfirm) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun GameProgressBar(questionNumber: Int, totalQuestions: Int) {
    val progress = if (totalQuestions > 0) questionNumber.toFloat() / totalQuestions else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Pregunta $questionNumber de $totalQuestions" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(CuidaBorderLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(CuidaGreen)
            )
        }
        Text(
            text = "$questionNumber de $totalQuestions",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaGreen
        )
    }
}

@Composable
private fun AnswerOptionCard(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (isSelected) CuidaGreenSurface else Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.5.dp,
                color = if (isSelected) CuidaGreen else CuidaBorderLight,
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                selected = isSelected
                stateDescription = if (isSelected) "Seleccionado" else "No seleccionado"
            }
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) CuidaGreenDark else CuidaTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrimaryActionButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = CuidaGreen,
            contentColor = Color.White,
            disabledContainerColor = CuidaBorderLight,
            disabledContentColor = CuidaTextDisabled
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}
