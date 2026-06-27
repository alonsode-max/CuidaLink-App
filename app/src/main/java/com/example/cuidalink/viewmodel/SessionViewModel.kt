package com.example.cuidalink.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.data.SessionStore
import com.example.cuidalink.model.Caretaker
import com.example.cuidalink.model.Patient
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

/** Rol del usuario que decide qué vista se muestra. */
enum class UserRole { PACIENTE, CUIDADOR }

/** Sesión observada por la UI: si hay login activo y con qué rol. */
data class SessionUiState(
    val isLoggedIn: Boolean = false,
    val role: UserRole = UserRole.PACIENTE
)

/** Estado del inicio de sesión para la pantalla de login. */
sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data class Success(val role: UserRole) : LoginState
    data class Error(val message: String) : LoginState
}

/** Sesion del usuario y fuente de verdad del rol; se guarda en DataStore. */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SessionStore(application)
    private val profileService = ProfileService()

    /** Estado de la sesion; null mientras se lee el DataStore (splash inicial). */
    val uiState: StateFlow<SessionUiState?> = store.session.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    /**
     * Inicia sesión autenticando contra Supabase y resuelve el rol por la tabla.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = runCatching {
                SupabaseConfig.client.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                resolveRole()
            }
            _loginState.value = result.fold(
                onSuccess = { role ->
                    store.saveSession(role)
                    LoginState.Success(role)
                },
                onFailure = { 
                    Log.e("SessionViewModel", "Login error", it)
                    LoginState.Error(mapError(it)) 
                }
            )
        }
    }

    fun signUpPatient(
        email: String, pass: String, name: String, age: Int,
        bloodGroup: String, allergies: String, weight: Float, height: Float
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Registro en Auth con metadatos de usuario
                val authResponse = SupabaseConfig.client.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = pass
                    data = buildJsonObject {
                        put("name", name)
                        put("role", "patient")
                    }
                }

                val userId = authResponse?.id ?: throw Exception("No se recibió un ID de usuario tras el registro.")

                val patient = Patient(
                    uid = userId,
                    name = name,
                    email = email.trim(),
                    fcmToken = null,
                    age = age,
                    bloodGroup = bloodGroup,
                    allergies = allergies,
                    weight = weight,
                    height = height,
                    createdAt = Instant.now().toString()
                )

                SupabaseConfig.client.postgrest["patients"].insert(patient)
                
                store.saveSession(UserRole.PACIENTE)
                _loginState.value = LoginState.Success(UserRole.PACIENTE)
            } catch (e: Exception) {
                Log.e("SignUpPatient", "Error detallado: ${e.message}", e)
                _loginState.value = LoginState.Error(mapError(e))
            }
        }
    }

    fun signUpCaretaker(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val authResponse = SupabaseConfig.client.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = pass
                    data = buildJsonObject {
                        put("name", name)
                        put("role", "caretaker")
                    }
                }

                val userId = authResponse?.id ?: throw Exception("No se recibió un ID de usuario tras el registro.")
                
                val caretaker = Caretaker(
                    uid = userId,
                    name = name,
                    email = email.trim(),
                    fcmToken = null,
                    createdAt = Instant.now().toString()
                )

                SupabaseConfig.client.postgrest["caretakers"].insert(caretaker)
                
                store.saveSession(UserRole.CUIDADOR)
                _loginState.value = LoginState.Success(UserRole.CUIDADOR)
            } catch (e: Exception) {
                Log.e("SignUpCaretaker", "Error: ${e.message}", e)
                _loginState.value = LoginState.Error(mapError(e))
            }
        }
    }

    /** Resetea el estado tras navegar (evita re-disparar al volver al login). */
    fun consumeLogin() {
        if (_loginState.value !is LoginState.Loading) {
            _loginState.value = LoginState.Idle
        }
    }

    /** Cierra la sesión local y la de Supabase. */
    fun logout() {
        viewModelScope.launch {
            runCatching { SupabaseConfig.client.auth.signOut() }
            store.clear()
            _loginState.value = LoginState.Idle
        }
    }

    /** Determina el rol según en qué tabla está el uid autenticado. */
    private suspend fun resolveRole(): UserRole {
        if (profileService.fetchCurrentCaretaker() != null) return UserRole.CUIDADOR
        if (profileService.fetchCurrentPatient() != null) return UserRole.PACIENTE
        error("Tu cuenta no tiene un perfil asociado")
    }

    private fun mapError(e: Throwable): String {
        val message = e.message ?: ""
        return when {
            message.contains("Invalid login credentials", ignoreCase = true) -> "Correo o contraseña incorrectos"
            message.contains("already registered", ignoreCase = true) -> "Este correo ya está registrado"
            message.contains("Email not confirmed", ignoreCase = true) -> "Confirma tu correo electrónico"
            message.contains("network", ignoreCase = true) -> "Sin conexión a internet"
            else -> "Error: ${e.localizedMessage ?: "Ocurrió un error inesperado"}"
        }
    }
}
