package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.repository.LinkRepository
import com.example.cuidalink.repository.SupabaseLinkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/** Estado del código del paciente (pantalla post-registro del paciente). */
sealed interface PatientCodeState {
    data object Loading : PatientCodeState
    data class Success(val code: String) : PatientCodeState
    data class Error(val message: String) : PatientCodeState
}

/** Estado de la vinculación (pantalla post-registro del cuidador). */
sealed interface LinkState {
    data object Idle : LinkState
    data object Loading : LinkState
    data object Success : LinkState
    data class Error(val message: String) : LinkState
}

/**
 * ViewModel compartido por las dos pantallas de vinculación:
 * el paciente muestra su código y el cuidador lo introduce para vincularse.
 */
class LinkViewModel(
    private val repository: LinkRepository = SupabaseLinkRepository()
) : ViewModel() {

    private val _patientCode = MutableStateFlow<PatientCodeState>(PatientCodeState.Loading)
    val patientCode: StateFlow<PatientCodeState> = _patientCode.asStateFlow()

    private val _codeInput = MutableStateFlow("")
    val codeInput: StateFlow<String> = _codeInput.asStateFlow()

    private val _linkState = MutableStateFlow<LinkState>(LinkState.Idle)
    val linkState: StateFlow<LinkState> = _linkState.asStateFlow()

    /** `true` cuando un cuidador ya vinculó a este paciente (lo detecta el sondeo). */
    private val _patientLinked = MutableStateFlow(false)
    val patientLinked: StateFlow<Boolean> = _patientLinked.asStateFlow()

    /** Recupera (o genera) el código del paciente; muestra carga mientras tanto. */
    fun loadMyCode() {
        viewModelScope.launch {
            _patientCode.value = PatientCodeState.Loading
            _patientCode.value = repository.getOrCreateMyPatientCode().fold(
                onSuccess = { PatientCodeState.Success(it) },
                onFailure = { PatientCodeState.Error(it.message ?: "No se pudo generar el código") }
            )
        }
    }

    /**
     * Sondea periódicamente si ya existe la vinculación del paciente. En cuanto un
     * cuidador lo vincula, pone [patientLinked] a true y la pantalla avanza sola.
     */
    fun watchForLink() {
        viewModelScope.launch {
            while (coroutineContext.isActive && !_patientLinked.value) {
                delay(LINK_POLL_INTERVAL_MS)
                val linked = repository.isCurrentPatientLinked().getOrDefault(false)
                if (linked) _patientLinked.value = true
            }
        }
    }

    /** El cuidador escribe el código: solo dígitos, máximo 6. */
    fun onCodeInputChange(value: String) {
        _codeInput.value = value.filter(Char::isDigit).take(CODE_LENGTH)
        if (_linkState.value is LinkState.Error) _linkState.value = LinkState.Idle
    }

    /** Indica si el código introducido tiene la longitud exacta. */
    fun isCodeComplete(): Boolean = _codeInput.value.length == CODE_LENGTH

    /** Envía el POST de vinculación con el código actual. */
    fun submitLink() {
        val code = _codeInput.value
        if (code.length != CODE_LENGTH) {
            _linkState.value = LinkState.Error("Introduce los $CODE_LENGTH dígitos del código")
            return
        }
        viewModelScope.launch {
            _linkState.value = LinkState.Loading
            _linkState.value = repository.linkPatientByCode(code).fold(
                onSuccess = { LinkState.Success },
                onFailure = { LinkState.Error(mapError(it)) }
            )
        }
    }

    private fun mapError(e: Throwable): String = when (e) {
        is NoSuchElementException -> "No encontramos ningún paciente con ese código"
        is IllegalArgumentException -> e.message ?: "Código no válido"
        is IllegalStateException -> e.message ?: "No hay sesión activa"
        else -> e.message ?: "No se pudo completar la vinculación"
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val LINK_POLL_INTERVAL_MS = 4_000L
    }
}
