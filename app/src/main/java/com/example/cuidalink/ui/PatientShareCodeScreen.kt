package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.components.ProfileErrorView
import com.example.cuidalink.ui.components.QrCodeImage
import com.example.cuidalink.ui.components.ShimmerBox
import com.example.cuidalink.ui.theme.CuidaBorder
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.viewmodel.LinkViewModel
import com.example.cuidalink.viewmodel.PatientCodeState

/**
 * Pantalla post-registro del PACIENTE: muestra su código de vinculación de
 * 6 dígitos en grande y su QR equivalente, con estado de carga mientras se
 * genera o recupera del backend.
 */
@Composable
fun PatientShareCodeScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    viewModel: LinkViewModel = viewModel()
) {
    val state by viewModel.patientCode.collectAsState()
    val linked by viewModel.patientLinked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyCode()
        viewModel.watchForLink()
    }
    // Cuando un cuidador crea la vinculación, avanzamos solos al panel.
    LaunchedEffect(linked) {
        if (linked) onContinue()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ShareCodeHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Tu código de vinculación",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Compártelo con tu cuidador para que pueda acompañarte.",
                fontSize = 15.sp,
                color = CuidaTextSecondary,
                textAlign = TextAlign.Center
            )

            when (val current = state) {
                is PatientCodeState.Loading -> LoadingCode()
                is PatientCodeState.Error -> ProfileErrorView(
                    message = current.message,
                    onRetry = { viewModel.loadMyCode() }
                )
                is PatientCodeState.Success -> CodeContent(current.code)
            }

            Box(modifier = Modifier.weight(1f, fill = true))

            // Sin botón "Continuar": no se avanza hasta que exista la vinculación.
            // La pantalla espera y se mueve sola cuando el cuidador escanea el código.
            if (state is PatientCodeState.Success) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = CuidaGreen,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "  Esperando a que tu cuidador te vincule…",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CuidaTextSecondary
                    )
                }
            }
        }
    }
}

/** Código grande + QR cuando ya está disponible. */
@Composable
private fun CodeContent(code: String) {
    // Código en dígitos grandes con espaciado generoso para lectura fácil.
    Text(
        text = code,
        fontSize = 52.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 8.sp,
        color = CuidaGreen,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CuidaGreenSurface)
            .padding(vertical = 20.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, CuidaBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        QrCodeImage(data = code, modifier = Modifier.fillMaxSize())
    }
}

/** Placeholder de carga: shimmer en el bloque del código y en el QR. */
@Composable
private fun LoadingCode() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp)
    )
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ShareCodeHeader() {
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
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "¡Cuenta creada!",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
