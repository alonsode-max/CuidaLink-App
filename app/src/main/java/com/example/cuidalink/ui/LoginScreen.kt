package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaHeaderGradientEnd
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.ui.theme.LocalDarkThemeActive
import com.example.cuidalink.ui.theme.keepOriginalColorsInDark

/** Pantalla de inicio de sesion: valida credenciales y enruta por rol. */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: (email: String, password: String) -> Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LoginHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 26.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Iniciar sesión",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    showError = false
                },
                label = { Text("Correo") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    showError = false
                },
                label = { Text("Contraseña") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null)
                },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val desc = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = desc)
                    }
                },
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            if (showError) {
                Text(
                    text = "Correo o contraseña incorrectos.",
                    color = CuidaRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    val ok = onLogin(email, password)
                    showError = !ok
                },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CuidaGreen,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Entrar", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }

            DemoCredentialsHint()
        }
    }
}

// Cabecera verde de marca con icono y nombre de la app.
@Composable
private fun LoginHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .keepOriginalColorsInDark()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CuidaGreen,
                        CuidaGreen,
                        if (LocalDarkThemeActive.current) Color(0xFF262729) else CuidaHeaderGradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = "CuidaLink",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Acompañando cada día",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Recordatorio visible de las cuentas de demo (frontend sin backend).
@Composable
private fun DemoCredentialsHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CuidaGreenSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Cuentas de prueba",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CuidaTextPrimary
        )
        DemoCredentialRow(label = "Paciente", value = "paciente@cuidalink.com · 1234")
        DemoCredentialRow(label = "Cuidador", value = "cuidador@cuidalink.com · 1234")
    }
}

@Composable
private fun DemoCredentialRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CuidaTextPrimary,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = CuidaTextSecondary
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CuidaGreen,
    focusedLabelColor = CuidaGreen,
    focusedLeadingIconColor = CuidaGreen,
    cursorColor = CuidaGreen
)
