package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.network.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidad de la cuenta actualmente logueada (paciente o cuidador), para mostrar
 * el nombre y las iniciales reales en la pantalla de Configuración.
 */
class AccountViewModel(
    private val service: ProfileService = ProfileService()
) : ViewModel() {

    data class Account(val name: String, val initials: String)

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val name = service.fetchCurrentPatient()?.name
                ?: service.fetchCurrentCaretaker()?.name
            if (name != null) {
                _account.value = Account(name, initialsOf(name))
            }
        }
    }

    private fun initialsOf(name: String): String =
        name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}
