package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.remote.EventCompletionRow
import com.example.cuidalink.model.remote.LocationHistoryRow
import com.example.cuidalink.network.ProfileService
import com.example.cuidalink.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Historial del cuidador: las tareas/recordatorios que el paciente (o el propio
 * cuidador) ha marcado como completados, leídos de `event_completions`, más el
 * historial de ubicaciones del paciente (para abrir cada punto en Google Maps).
 */
class HistoryViewModel(
    private val repository: CalendarRepository = CalendarRepository(),
    private val profileService: ProfileService = ProfileService()
) : ViewModel() {

    private val _items = MutableStateFlow<List<EventCompletionRow>>(emptyList())
    val items: StateFlow<List<EventCompletionRow>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _locations = MutableStateFlow<List<LocationHistoryRow>>(emptyList())
    val locations: StateFlow<List<LocationHistoryRow>> = _locations.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            val id = repository.resolvePatientId()
            if (id != null) {
                repository.fetchCompletions(id).onSuccess { _items.value = it }
            }
            val patientId = profileService.fetchSessionPatient()?.id
            if (patientId != null) {
                runCatching { profileService.fetchLocationHistory(patientId) }
                    .onSuccess { _locations.value = it }
            }
            _isLoading.value = false
        }
    }
}
