package com.example.cuidalink.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.data.SessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Rol del usuario que decide qué vista se muestra. */
enum class UserRole { PACIENTE, CUIDADOR }

/** Sesión observada por la UI: si hay login activo y con qué rol. */
data class SessionUiState(
    val isLoggedIn: Boolean = false,
    val role: UserRole = UserRole.PACIENTE
)

/** Sesion del usuario y fuente de verdad del rol; se guarda en DataStore. */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SessionStore(application)

    /** Estado de la sesion; null mientras se lee el DataStore (splash inicial). */
    val uiState: StateFlow<SessionUiState?> = store.session.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    /** Valida credenciales demo; si coinciden guarda la sesion y devuelve el rol. */
    fun authenticate(email: String, password: String): UserRole? {
        val account = DEMO_ACCOUNTS[email.trim().lowercase()] ?: return null
        if (account.password != password) return null
        viewModelScope.launch { store.saveSession(account.role) }
        return account.role
    }

    /** Cierra la sesión (borra la sesión guardada). */
    fun logout() {
        viewModelScope.launch { store.clear() }
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
