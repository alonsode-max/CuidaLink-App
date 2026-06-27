package com.example.cuidalink.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.data.SessionStore
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
     * Inicia sesión. Primero intenta las cuentas demo (offline / antes del backend);
     * si no coinciden, autentica contra Supabase y resuelve el rol por la tabla.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // Fallback demo (frontend sin backend). TODO: quitar al conectar el backend real.
            val demo = DEMO_ACCOUNTS[email.trim().lowercase()]
            if (demo != null && demo.password == password) {
                store.saveSession(demo.role)
                _loginState.value = LoginState.Success(demo.role)
                return@launch
            }

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
                onFailure = { LoginState.Error(mapError(it)) }
            )
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
        error("El usuario no está en patients ni caretakers")
    }

    private fun mapError(e: Throwable): String {
        val message = e.message ?: ""
        return when {
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "Correo o contraseña incorrectos"
            message.contains("Email not confirmed", ignoreCase = true) ->
                "Confirma tu correo antes de entrar"
            message.contains("patients ni caretakers", ignoreCase = true) ->
                "Tu cuenta no tiene un rol asignado"
            message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("network", ignoreCase = true) ->
                "Sin conexión a internet"
            else -> "No se pudo iniciar sesión"
        }
    }

    private data class DemoAccount(val password: String, val role: UserRole)

    companion object {
        /** Cuentas demo (frontend, sin backend); la clave es el correo en minusculas. */
        private val DEMO_ACCOUNTS = mapOf(
            "paciente@cuidalink.com" to DemoAccount("1234", UserRole.PACIENTE),
            "cuidador@cuidalink.com" to DemoAccount("1234", UserRole.CUIDADOR)
        )
    }
}
