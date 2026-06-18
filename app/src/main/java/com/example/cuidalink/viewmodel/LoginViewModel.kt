package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena todos los campos")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Realizamos el registro
                SupabaseConfig.client.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }

                // IMPORTANTE: Forzamos el cierre de sesión inmediata por si Supabase
                // nos ha logueado automáticamente (ocurre si no hay confirmación por email).
                // Esto evita que el usuario "salte" dentro de la app sin querer.
                SupabaseConfig.client.auth.signOut()

                // Cambiamos el estado a Success para mostrar el mensaje y quedarnos en la pantalla de Login
                _authState.value = AuthState.Success(
                    "¡Registro completado! Por favor, ahora introduce tus datos arriba e inicia sesión."
                )
            } catch (e: Exception) {
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
            _authState.value = AuthState.Idle
        }
    }

    private fun mapError(e: Exception): String {
        val message = e.message ?: ""
        // Imprimimos el error en el Logcat para verlo mientras programas
        android.util.Log.e("SupabaseError", "Error detectado: $message")

        return when {
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "Email o contraseña incorrectos"
            // Captura el error de duplicado (solo si desactivas la protección en Attack Protection)
            message.contains("already registered", ignoreCase = true) ||
            message.contains("already exists", ignoreCase = true) ->
                "Este correo ya está registrado"
            message.contains("Email not confirmed", ignoreCase = true) ->
                "Por favor, confirma tu email antes de entrar"
            message.contains("Unable to resolve host", ignoreCase = true) || 
            message.contains("network", ignoreCase = true) ->
                "Sin conexión a internet"
            else -> "Error: $message"
        }
    }
}
