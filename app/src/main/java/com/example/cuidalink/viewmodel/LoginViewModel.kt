package com.example.cuidalink.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.Caretaker
import com.example.cuidalink.model.Patient
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class LoginViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        // Observamos el estado de la sesión automáticamente para persistencia.
        // Solo actualizamos a Authenticated si no estamos en medio de un proceso (Loading).
        viewModelScope.launch {
            SupabaseConfig.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        if (_authState.value !is AuthState.Loading) {
                            _authState.value = AuthState.Authenticated
                        }
                    }
                    is SessionStatus.NotAuthenticated -> _authState.value = AuthState.Idle
                    else -> {}
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                SupabaseConfig.client.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun signUpPatient(
        email: String, pass: String, name: String, age: Int,
        bloodGroup: String, allergies: String, weight: Float, height: Float
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResponse = SupabaseConfig.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    data = buildJsonObject {
                        put("name", name)
                        put("role", "patient")
                        put("age", age)
                        put("blood_group", bloodGroup)
                        put("allergies", allergies)
                        put("weight", weight)
                        put("height", height)
                    }
                }

                val userId = authResponse?.id ?: throw Exception("Error al obtener ID")

                val patient = Patient(
                    uid = userId,
                    name = name,
                    email = email,
                    fcmToken = "token_placeholder",
                    age = age,
                    bloodGroup = bloodGroup,
                    allergies = allergies,
                    weight = weight,
                    height = height,
                    createdAt = Instant.now().toString()
                )

                try {
                    SupabaseConfig.client.postgrest["patients"].insert(patient)
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    if (!errorMsg.contains("duplicate key", ignoreCase = true)) {
                        throw e
                    }
                }
                // Ahora que la base de datos está lista, pasamos a Authenticated
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun signUpCaretaker(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena los campos obligatorios")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResponse = SupabaseConfig.client.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                    data = buildJsonObject {
                        put("name", name)
                        put("role", "caretaker")
                    }
                }

                val userId = authResponse?.id ?: throw Exception("Error al obtener ID de usuario")
                val now = Instant.now().toString()
                val fcmToken = "token_placeholder"

                val caretaker = Caretaker(
                    uid = userId,
                    name = name,
                    email = email,
                    fcmToken = fcmToken,
                    createdAt = now
                )

                try {
                    SupabaseConfig.client.postgrest["caretakers"].insert(caretaker)
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    if (!errorMsg.contains("duplicate key", ignoreCase = true)) {
                        throw e
                    }
                }
                // Ahora que la base de datos está lista, pasamos a Authenticated
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                Log.e("SignUp", "Error: ${e.message}", e)
                _authState.value = AuthState.Error(mapError(e))
            }
        }
    }

    fun checkSession() {
        val user = SupabaseConfig.client.auth.currentUserOrNull()
        if (user != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            SupabaseConfig.client.auth.signOut()
            // sessionStatus actualizará _authState a Idle
        }
    }

    private fun mapError(e: Exception): String {
        val message = e.message ?: ""
        android.util.Log.e("SupabaseError", "Error detectado: $message")

        return when {
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "Email o contraseña incorrectos"
            message.contains("already registered", ignoreCase = true) ||
            message.contains("already exists", ignoreCase = true) ->
                "Este correo ya está registrado"
            message.contains("duplicate key", ignoreCase = true) ->
                "Este registro ya existe en la base de datos"
            message.contains("violates row-level security policy", ignoreCase = true) ||
            message.contains("42501", ignoreCase = true) ||
            message.contains("operator does not exist", ignoreCase = true) ->
                "Error de permisos (RLS) o tipos. Verifica que la política use ::text en auth.uid()."
            message.contains("Email not confirmed", ignoreCase = true) ->
                "Por favor, confirma tu email antes de entrar"
            message.contains("Unable to resolve host", ignoreCase = true) || 
            message.contains("network", ignoreCase = true) ->
                "Sin conexión a internet"
            else -> "Error: $message"
        }
    }
}
