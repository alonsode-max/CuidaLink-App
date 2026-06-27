package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.ui.CaregiverProfileUi
import com.example.cuidalink.repository.ProfileRepository
import com.example.cuidalink.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Expone el perfil del propio cuidador como StateFlow para las tarjetas Bento. */
class CaregiverProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState<CaregiverProfileUi>>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState<CaregiverProfileUi>> = _state.asStateFlow()

    init {
        loadCurrentCaregiver()
    }

    /** Carga el perfil del cuidador vinculado a la sesión actual. */
    fun loadCurrentCaregiver() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            _state.value = repository.getCurrentCaregiverProfile().fold(
                onSuccess = { ProfileUiState.Success(it) },
                onFailure = { ProfileUiState.Error(it.message ?: "No se pudo cargar el perfil") }
            )
        }
    }
}
