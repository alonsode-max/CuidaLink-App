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
import com.example.cuidalink.repository.LinkRepository
import com.example.cuidalink.repository.SupabaseLinkRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.delay
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
    private val linkRepository: LinkRepository = SupabaseLinkRepository()

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

    /**
     * `true` si el paciente ya tiene un cuidador vinculado. Se usa tras el login
     * para decidir si mostrar la pantalla del código (solo si aún NO está vinculado).
     * Ante cualquier error asume que NO está vinculado, para mostrar la pantalla
     * de vinculación en vez de saltarla al home.
     */
    suspend fun isPatientLinked(): Boolean =
        linkRepository.isCurrentPatientLinked().getOrDefault(false)

    /** Igual que [isPatientLinked] pero para el cuidador (ya tiene paciente). */
    suspend fun isCaretakerLinked(): Boolean =
        linkRepository.isCurrentCaretakerLinked().getOrDefault(false)

    /**
     * Resuelve el rol del usuario YA autenticado. Primero intenta las cuentas demo;
     * luego usa el `role` del metadata de Auth; por último cae a mirar en qué tabla 
     * vive su uid. Devuelve null si no se puede determinar.
     * Incluye reintentos para evitar carreras en el registro.
     */
    suspend fun resolveCurrentRole(): UserRole? {
        // 1. Verificar si es una cuenta demo por el email de la sesión de Supabase
        val email = SupabaseConfig.client.auth.currentUserOrNull()?.email?.lowercase()
        if (email != null && (email == "cuidador@cuidalink.com" || email == "paciente@cuidalink.com")) {
            return if (email.contains("cuidador")) UserRole.CUIDADOR else UserRole.PACIENTE
        }

        repeat(3) { attempt ->
            val metaRole = runCatching {
                SupabaseConfig.client.auth.currentUserOrNull()
                    ?.userMetadata?.get("role")?.jsonPrimitive?.content
            }.getOrNull()

            when (metaRole) {
                "caretaker" -> return UserRole.CUIDADOR
                "patient" -> return UserRole.PACIENTE
            }

            val dbRole = runCatching {
                // Comprobación directa contra Supabase para evitar fallos de caché
                val uid = SupabaseConfig.client.auth.currentUserOrNull()?.id
                if (uid != null) {
                    when {
                        profileService.fetchCaretakerByUid(uid) != null -> UserRole.CUIDADOR
                        profileService.fetchPatientByUid(uid) != null -> UserRole.PACIENTE
                        else -> null
                    }
                } else null
            }.getOrNull()

            if (dbRole != null) return dbRole
            
            // Si es el primer o segundo intento y no hay rol, esperamos un poco (carrera en registro)
            if (attempt < 2) delay(1000)
        }
        return null
    }

    /** Guarda en disco la sesión local con el rol indicado (persistencia). */
    fun persistSession(role: UserRole) {
        viewModelScope.launch { store.saveSession(role) }
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
