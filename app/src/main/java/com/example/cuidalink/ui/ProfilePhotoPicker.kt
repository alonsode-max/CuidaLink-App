package com.example.cuidalink.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Devuelve una acción que abre el selector de fotos del sistema (Android Photo Picker,
 * sin permisos de almacenamiento) y entrega los bytes de la imagen elegida.
 *
 * Pensado para reutilizarse en las pantallas de perfil del paciente y del cuidador.
 */
@Composable
fun rememberImagePicker(onImageBytes: (ByteArray) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()?.let(onImageBytes)
        }
    }
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
