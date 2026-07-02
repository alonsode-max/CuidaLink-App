package com.example.cuidalink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuidalink.repository.RegistrationRepository
import com.example.cuidalink.repository.SupabaseRegistrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Datos del formulario de registro recogidos desde la UI. */
data class RegisterFormState(
    val role: UserRole = UserRole.CUIDADOR,
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val emergencyPhone: String = "",
    val password: String = ""
)

/** Estado del envío del registro al repositorio. */
sealed interface RegisterSubmissionState {
    data object Idle : RegisterSubmissionState
    data object Loading : RegisterSubmissionState
    data object Success : RegisterSubmissionState
    data class Error(val message: String) : RegisterSubmissionState
}

/** Recoge el formulario de registro y simula el alta contra el repositorio. */
class RegisterViewModel(
    private val repository: RegistrationRepository = SupabaseRegistrationRepository()
) : ViewModel() {

    private val _form = MutableStateFlow(RegisterFormState())
    val form: StateFlow<RegisterFormState> = _form.asStateFlow()

    private val _submission = MutableStateFlow<RegisterSubmissionState>(RegisterSubmissionState.Idle)
    val submission: StateFlow<RegisterSubmissionState> = _submission.asStateFlow()

    fun onRoleChange(role: UserRole) = _form.update { it.copy(role = role) }
    fun onNameChange(value: String) = _form.update { it.copy(name = value) }
    fun onEmailChange(value: String) = _form.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _form.update { it.copy(password = value) }

    /** El teléfono solo admite dígitos, espacios y '+' (máx. 15 caracteres). */
    fun onEmergencyPhoneChange(value: String) {
        val cleaned = value.filter { it.isDigit() || it == '+' || it == ' ' }.take(15)
        _form.update { it.copy(emergencyPhone = cleaned) }
    }

    /** La edad solo admite dígitos (máx. 3). */
    fun onAgeChange(value: String) {
        val digits = value.filter(Char::isDigit).take(3)
        _form.update { it.copy(age = digits) }
    }

    /** Indica si el formulario tiene los campos mínimos para el rol elegido. */
    fun isFormValid(form: RegisterFormState): Boolean {
        val baseValid = form.name.isNotBlank() &&
            form.email.isNotBlank() &&
            form.password.isNotBlank()
        return when (form.role) {
            UserRole.CUIDADOR -> baseValid
            UserRole.PACIENTE -> baseValid &&
                (form.age.toIntOrNull() ?: 0) > 0 &&
                form.emergencyPhone.filter(Char::isDigit).length >= 7
        }
    }

    /** Envía los campos exactos al repositorio mostrando el estado de carga. */
    fun register() {
        val current = _form.value
        if (!isFormValid(current)) {
            _submission.value = RegisterSubmissionState.Error("Completa todos los campos obligatorios")
            return
        }

        viewModelScope.launch {
            _submission.value = RegisterSubmissionState.Loading
            val result = when (current.role) {
                UserRole.CUIDADOR -> repository.registerCaregiver(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    password = current.password
                )
                UserRole.PACIENTE -> repository.registerPatient(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    age = current.age.toInt(),
                    emergencyPhone = current.emergencyPhone.trim(),
                    password = current.password
                )
            }
            _submission.value = result.fold(
                onSuccess = { RegisterSubmissionState.Success },
                onFailure = { RegisterSubmissionState.Error(it.message ?: "No se pudo crear la cuenta") }
            )
        }
    }

    /** Vuelve el envío a Idle (al editar tras un error). */
    fun resetSubmission() {
        if (_submission.value !is RegisterSubmissionState.Loading) {
            _submission.value = RegisterSubmissionState.Idle
        }
    }
}
