package com.example.cuidalink.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaGreenSurfaceHover
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.viewmodel.LinkState
import com.example.cuidalink.viewmodel.LinkViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** Las dos formas de vincular: escanear el QR o teclear el código. */
private enum class LinkMode { SCAN, CODE }

/**
 * Pantalla post-registro del CUIDADOR. Ofrece una de dos vías para vincular:
 * escanear el QR del paciente (cámara) o introducir sus 6 dígitos a mano.
 */
@Composable
fun CaretakerLinkScreen(
    modifier: Modifier = Modifier,
    onLinked: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: LinkViewModel = viewModel()
) {
    val code by viewModel.codeInput.collectAsState()
    val linkState by viewModel.linkState.collectAsState()
    val isLoading = linkState is LinkState.Loading
    var mode by remember { mutableStateOf(LinkMode.SCAN) }

    // Escáner de cámara: el QR del paciente contiene su código de 6 dígitos.
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) {
            viewModel.onCodeInputChange(contents)
            viewModel.submitLink()
        }
    }

    LaunchedEffect(linkState) {
        if (linkState is LinkState.Success) onLinked()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LinkHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Vincula a tu paciente",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )

            ModeSelector(selected = mode, onSelect = { mode = it })

            when (mode) {
                LinkMode.SCAN -> ScanSection(
                    enabled = !isLoading,
                    onScan = {
                        scanLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt("Apunta al código QR del paciente")
                                setBeepEnabled(false)
                                setOrientationLocked(false)
                            }
                        )
                    }
                )
                LinkMode.CODE -> CodeSection(
                    code = code,
                    enabled = !isLoading,
                    isLoading = isLoading,
                    canSubmit = viewModel.isCodeComplete(),
                    onCodeChange = viewModel::onCodeInputChange,
                    onSubmit = { viewModel.submitLink() }
                )
            }

            val errorState = linkState as? LinkState.Error
            if (errorState != null) {
                Text(
                    text = errorState.message,
                    color = CuidaRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Selector segmentado de las dos vías de vinculación. */
@Composable
private fun ModeSelector(selected: LinkMode, onSelect: (LinkMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaGreenSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModePill("Escanear QR", selected == LinkMode.SCAN, { onSelect(LinkMode.SCAN) }, Modifier.weight(1f))
        ModePill("Código", selected == LinkMode.CODE, { onSelect(LinkMode.CODE) }, Modifier.weight(1f))
    }
}

@Composable
private fun ModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (isSelected) CuidaGreen else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else CuidaTextSecondary
        )
    }
}

/** Vía 1: recuadro que abre la cámara para escanear el QR. */
@Composable
private fun ScanSection(enabled: Boolean, onScan: () -> Unit) {
    Text(
        text = "Apunta la cámara al código QR que muestra el paciente.",
        fontSize = 15.sp,
        color = CuidaTextSecondary
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(CuidaGreenSurface)
            .clickable(enabled = enabled, onClick = onScan),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = CuidaGreen,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Toca para abrir la cámara",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CuidaTextSecondary
            )
        }
    }
}

/** Vía 2: campo Bento de 6 dígitos + botón Vincular. */
@Composable
private fun CodeSection(
    code: String,
    enabled: Boolean,
    isLoading: Boolean,
    canSubmit: Boolean,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Text(
        text = "Introduce los 6 dígitos del código del paciente.",
        fontSize = 15.sp,
        color = CuidaTextSecondary
    )
    OutlinedTextField(
        value = code,
        onValueChange = onCodeChange,
        label = { Text("Código de 6 dígitos") },
        singleLine = true,
        leadingIcon = { Icon(imageVector = Icons.Filled.CenterFocusWeak, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CuidaGreen,
            focusedLabelColor = CuidaGreen,
            focusedLeadingIconColor = CuidaGreen,
            cursorColor = CuidaGreen
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSubmit,
        enabled = enabled && canSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = CuidaGreen,
            contentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text = "Vincular", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun LinkHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(CuidaGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "Conecta con tu paciente",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
