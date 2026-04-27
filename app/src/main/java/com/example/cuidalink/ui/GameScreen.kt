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
            Text("Se necesita permiso para leer contactos")
            Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                Text("Conceder Permiso")
            }
        } else if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.isGameOver) {
            Text("¡Juego Terminado!", style = MaterialTheme.typography.headlineMedium)
            Text("Puntuación Final: ${state.score}", modifier = Modifier.padding(vertical = 8.dp))
            state.message?.let { Text(it) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.fetchContacts(context.contentResolver) }) {
                Text("Reiniciar")
            }
        } else {
            state.currentContact?.let { contact ->
                Text("¿Quién es?", style = MaterialTheme.typography.headlineMedium)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(200.dp)
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
                    Text(
                        text = it,
                        color = if (it.startsWith("¡Correcto!")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Puntuación: ${state.score}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
