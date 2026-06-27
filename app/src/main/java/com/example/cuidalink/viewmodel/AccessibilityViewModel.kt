package com.example.cuidalink.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.data.AccessibilitySettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de accesibilidad observado por la UI (respaldado por DataStore). */
/** Preferencia de tema: claro fijo, oscuro fijo o seguir al sistema. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class AccessibilityUiState(
    val isHighContrastEnabled: Boolean = false,
    val textSizeMultiplier: Float = 1.0f,
    val reduceAnimations: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/** Conecta el DataStore con la UI: expone el estado y ofrece acciones. */
class AccessibilityViewModel(application: Application) : AndroidViewModel(application) {

    private val store = AccessibilitySettingsStore(application)

    val uiState: StateFlow<AccessibilityUiState> = store.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccessibilityUiState()
    )

    fun toggleHighContrast(enabled: Boolean) {
        viewModelScope.launch { store.setHighContrast(enabled) }
    }

    fun updateTextSize(multiplier: Float) {
        viewModelScope.launch { store.setTextSize(multiplier) }
    }

    fun toggleAnimations(reduce: Boolean) {
        viewModelScope.launch { store.setReduceAnimations(reduce) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }
}
