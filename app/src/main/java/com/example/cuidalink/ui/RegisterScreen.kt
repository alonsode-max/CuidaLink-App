package com.example.cuidalink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cuidalink.ui.theme.CuidaGreen
import com.example.cuidalink.ui.theme.CuidaGreenSurface
import com.example.cuidalink.ui.theme.CuidaRed
import com.example.cuidalink.ui.theme.CuidaTextPrimary
import com.example.cuidalink.ui.theme.CuidaTextSecondary
import com.example.cuidalink.viewmodel.RegisterSubmissionState
import com.example.cuidalink.viewmodel.RegisterViewModel
import com.example.cuidalink.viewmodel.UserRole

/** Pantalla de registro: elige rol y pide solo los campos obligatorios. */
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegistered: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val form by viewModel.form.collectAsState()
    val submission by viewModel.submission.collectAsState()
    val isLoading = submission is RegisterSubmissionState.Loading
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(submission) {
        if (submission is RegisterSubmissionState.Success) onRegistered()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RegisterHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crear cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CuidaTextPrimary
            )

            RoleSelector(
                selected = form.role,
                onSelect = {
                    viewModel.onRoleChange(it)
                    viewModel.resetSubmission()
                }
            )

            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.onNameChange(it); viewModel.resetSubmission() },
                label = { Text("Nombre") },
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Filled.Badge, contentDescription = null) },
                enabled = !isLoading,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.email,
                onValueChange = { viewModel.onEmailChange(it); viewModel.resetSubmission() },
                label = { Text("Correo") },
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Filled.Mail, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            if (form.role == UserRole.PACIENTE) {
                OutlinedTextField(
                    value = form.age,
                    onValueChange = { viewModel.onAgeChange(it); viewModel.resetSubmission() },
                    label = { Text("Edad") },
                    singleLine = true,
                    leadingIcon = { Icon(imageVector = Icons.Filled.Cake, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading,
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = form.password,
                onValueChange = { viewModel.onPasswordChange(it); viewModel.resetSubmission() },
                label = { Text("Contraseña") },
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
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
                enabled = !isLoading,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            val errorState = submission as? RegisterSubmissionState.Error
            if (errorState != null) {
                Text(
                    text = errorState.message,
                    color = CuidaRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { viewModel.register() },
                enabled = !isLoading && viewModel.isFormValid(form),
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
                    Text(text = "Crear Cuenta", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/** Selector segmentado de rol (dos píldoras), touch target ≥ 48.dp. */
@Composable
private fun RoleSelector(selected: UserRole, onSelect: (UserRole) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(CuidaGreenSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RolePill(
            label = "Cuidador",
            isSelected = selected == UserRole.CUIDADOR,
            onClick = { onSelect(UserRole.CUIDADOR) },
            modifier = Modifier.weight(1f)
        )
        RolePill(
            label = "Paciente",
            isSelected = selected == UserRole.PACIENTE,
            onClick = { onSelect(UserRole.PACIENTE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RolePill(
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

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(CuidaGreen)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al inicio de sesión",
                tint = Color.White
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 20.dp),
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
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "Únete a CuidaLink",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CuidaGreen,
    focusedLabelColor = CuidaGreen,
    focusedLeadingIconColor = CuidaGreen,
    cursorColor = CuidaGreen
)
