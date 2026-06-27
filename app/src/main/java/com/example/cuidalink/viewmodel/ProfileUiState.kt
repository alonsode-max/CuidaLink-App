package com.example.cuidalink.viewmodel

/**
 * Estado de una pantalla de perfil: carga, éxito con datos reales o error.
 *
 * `Loading` alimenta los Shimmer Effects; `Success` lleva el data class ya mapeado
 * desde el backend; `Error` lleva un mensaje listo para mostrar al usuario.
 */
sealed interface ProfileUiState<out T> {
    data object Loading : ProfileUiState<Nothing>
    data class Success<T>(val data: T) : ProfileUiState<T>
    data class Error(val message: String) : ProfileUiState<Nothing>
}
