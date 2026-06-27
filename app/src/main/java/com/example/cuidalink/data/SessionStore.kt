package com.example.cuidalink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cuidalink.viewmodel.SessionUiState
import com.example.cuidalink.viewmodel.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Persistencia local de la sesión (login simulado en frontend) con Preferences
private val Context.sessionDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "session")

/** Lee y escribe la sesión del usuario (si hay login activo y con qué rol). */
class SessionStore(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ROLE = stringPreferencesKey("user_role")
    }

    // Flujo reactivo con la sesión guardada (o los defaults si aún no hay nada:
    val session: Flow<SessionUiState> = context.sessionDataStore.data.map { prefs ->
        SessionUiState(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            role = prefs[Keys.ROLE]
                ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                ?: UserRole.PACIENTE
        )
    }

    suspend fun saveSession(role: UserRole) {
        context.sessionDataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.ROLE] = role.name
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit {
            it[Keys.IS_LOGGED_IN] = false
            it.remove(Keys.ROLE)
        }
    }
}
