package com.example.cuidalink.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cuidalink.viewmodel.LoginViewModel
import com.example.cuidalink.viewmodel.SessionViewModel
import com.example.cuidalink.viewmodel.LoginState

/** Grupos sanguíneos estándar para el selector del registro. */
private val BLOOD_GROUPS = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    sessionViewModel: SessionViewModel
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Campos adicionales para registro
    var name by remember { mutableStateOf("") }
    var isPatient by remember { mutableStateOf(true) }
    
    // Campos específicos de paciente
    var age by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    val sessionLoginState by sessionViewModel.loginState.collectAsState()
    val scrollState = rememberScrollState()

    val isLoading = sessionLoginState is LoginState.Loading
    val errorMsg = (sessionLoginState as? LoginState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CuidaLink", 
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isRegisterMode) "Crear cuenta" else "Bienvenido",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isRegisterMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isPatient,
                    onClick = { isPatient = true },
                    label = { Text("Paciente") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = !isPatient,
                    onClick = { isPatient = false },
                    label = { Text("Cuidador") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; sessionViewModel.consumeLogin() },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; sessionViewModel.consumeLogin() },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; sessionViewModel.consumeLogin() },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isRegisterMode && isPatient) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it; sessionViewModel.consumeLogin() },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = bloodGroupExpanded,
                onExpandedChange = { bloodGroupExpanded = !bloodGroupExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupo sanguíneo") },
                    placeholder = { Text("Selecciona") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = bloodGroupExpanded,
                    onDismissRequest = { bloodGroupExpanded = false }
                ) {
                    BLOOD_GROUPS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                bloodGroup = option
                                bloodGroupExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it; sessionViewModel.consumeLogin() },
                label = { Text("Alergias") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it; sessionViewModel.consumeLogin() },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it; sessionViewModel.consumeLogin() },
                    label = { Text("Altura (cm)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (isRegisterMode) {
                        if (isPatient) {
                            sessionViewModel.signUpPatient(
                                email = email,
                                pass = password,
                                name = name,
                                age = age.toIntOrNull() ?: 0,
                                bloodGroup = bloodGroup,
                                allergies = allergies,
                                weight = weight.toFloatOrNull() ?: 0f,
                                height = height.toFloatOrNull() ?: 0f
                            )
                        } else {
                            sessionViewModel.signUpCaretaker(email, password, name)
                        }
                    } else {
                        sessionViewModel.login(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Completar Registro" else "Iniciar Sesión")
            }
            
            TextButton(
                onClick = { 
                    isRegisterMode = !isRegisterMode 
                    sessionViewModel.consumeLogin()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
            }
        }

        errorMsg?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
