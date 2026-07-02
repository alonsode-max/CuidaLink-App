package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.model.remote.EventCompletionRow
import com.example.cuidalink.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Historial del cuidador: las tareas/recordatorios que el paciente (o el propio
 * cuidador) ha marcado como completados, leídos de `event_completions`.
 */
class HistoryViewModel(
    private val repository: CalendarRepository = CalendarRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<EventCompletionRow>>(emptyList())
    val items: StateFlow<List<EventCompletionRow>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
            _isLoading.value = false
        }
    }
}
