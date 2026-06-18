package com.example.cuidalink.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cuidalink.viewmodel.GameViewModel

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasContactPermission) {
            Text("Necesitamos ver tus contactos para poder jugar", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                Text("Permitir acceso")
            }
        } else if (state.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Preparando el juego...")
        } else if (state.isGameOver) {
            Text("¡Buen juego!", style = MaterialTheme.typography.headlineMedium)
            Text("Has conseguido ${state.score} puntos", modifier = Modifier.padding(vertical = 8.dp))
            state.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.fetchContacts(context.contentResolver) }) {
                Text("Volver a jugar")
            }
        } else {
            state.currentContact?.let { contact ->
                Text("¿Sabes quién es?", style = MaterialTheme.typography.headlineMedium)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = "Foto del contacto",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                state.options.forEach { option ->
                    Button(
                        onClick = { viewModel.checkGuess(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(option)
                    }
                }
                
                state.message?.let {
                    val isSuccess = it.contains("acertado") || it.contains("bien")
                    Text(
                        text = it,
                        color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Puntuación actual: ${state.score}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
