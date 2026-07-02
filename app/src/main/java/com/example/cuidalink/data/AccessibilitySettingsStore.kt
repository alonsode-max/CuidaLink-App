package com.example.cuidalink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cuidalink.viewmodel.AccessibilityUiState
import com.example.cuidalink.viewmodel.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Persistencia local de las preferencias de accesibilidad con Preferences
private val Context.accessibilityDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "accessibility_settings")

/** Lee y escribe las preferencias de accesibilidad en DataStore. */
class AccessibilitySettingsStore(private val context: Context) {

    private object Keys {
        val HIGH_CONTRAST = booleanPreferencesKey("is_high_contrast_enabled")
        val TEXT_SIZE = floatPreferencesKey("text_size_multiplier")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // Flujo reactivo con los valores actuales (o los defaults si aún no se han
    val settings: Flow<AccessibilityUiState> = context.accessibilityDataStore.data.map { prefs ->
        AccessibilityUiState(
            isHighContrastEnabled = prefs[Keys.HIGH_CONTRAST] ?: false,
            textSizeMultiplier = prefs[Keys.TEXT_SIZE] ?: 1.0f,
            reduceAnimations = prefs[Keys.REDUCE_ANIMATIONS] ?: false,
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        )
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.accessibilityDataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setTextSize(multiplier: Float) {
        context.accessibilityDataStore.edit { it[Keys.TEXT_SIZE] = multiplier }
    }

    suspend fun setReduceAnimations(reduce: Boolean) {
        context.accessibilityDataStore.edit { it[Keys.REDUCE_ANIMATIONS] = reduce }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.accessibilityDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
