package com.example.cuidalink.repository

import com.example.cuidalink.network.LinkService
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.random.Random

/** Tope de espera para las llamadas de red antes de degradar/fallar. */
private const val NETWORK_TIMEOUT_MS = 18_000L

/** Genera un código de vinculación de 6 dígitos (100000–999999). */
internal fun generateLinkCode(): String = Random.nextInt(100_000, 1_000_000).toString()

/**
 * Lógica de vinculación paciente ↔ cuidador.
 *
 * - El paciente obtiene (o genera) su código único de 6 dígitos.
 * - El cuidador introduce ese código y crea el registro en `vinculations`.
 *
 * Devuelve `Result` para manejo explícito de error en la UI.
 */
interface LinkRepository {

    /** Código del paciente autenticado; lo genera y persiste si aún no existe. */
    suspend fun getOrCreateMyPatientCode(): Result<String>

    /** Vincula al cuidador actual con el paciente dueño de `code`. */
    suspend fun linkPatientByCode(code: String): Result<Unit>

    /** `true` si el paciente autenticado ya tiene un cuidador vinculado. */
    suspend fun isCurrentPatientLinked(): Result<Boolean>

    /** `true` si el cuidador autenticado ya tiene un paciente vinculado. */
    suspend fun isCurrentCaretakerLinked(): Result<Boolean>

    /** Rompe el vínculo del usuario actual (paciente o cuidador). */
    suspend fun unlinkCurrent(): Result<Unit>
}

/**
 * Implementación real contra Supabase.
 *
 * Si no hay sesión activa (p. ej. cuentas demo del frontend) degrada con
 * elegancia: el código del paciente se genera localmente y el vínculo se simula,
 * de modo que el flujo de UI funciona sin backend.
 */
class SupabaseLinkRepository(
    private val service: LinkService = LinkService()
) : LinkRepository {

    override suspend fun getOrCreateMyPatientCode(): Result<String> = runCatching {
        val uid = service.currentUid() ?: return@runCatching generateLinkCode()

        try {
            withTimeout(NETWORK_TIMEOUT_MS) {
                val existing = service.fetchPatientByUid(uid)?.code
                if (!existing.isNullOrBlank()) {
                    existing
                } else {
                    val newCode = generateLinkCode()
                    service.assignPatientCode(uid, newCode)
                    newCode
                }
            }
        } catch (e: TimeoutCancellationException) {
            // Red colgada: no bloqueamos el alta, mostramos un código local.
            generateLinkCode()
        }
    }

    override suspend fun linkPatientByCode(code: String): Result<Unit> = runCatching {
        require(code.length == CODE_LENGTH && code.all(Char::isDigit)) {
            "El código debe tener $CODE_LENGTH dígitos"
        }

        try {
            withTimeout(NETWORK_TIMEOUT_MS) {
                val patient = service.fetchPatientByCode(code)
                    ?: throw NoSuchElementException("No existe ningún paciente con ese código")
                val patientId = patient.id
                    ?: throw IllegalStateException("El paciente no tiene identificador en el backend")

                val caretakerUid = service.currentUid()
                    ?: throw IllegalStateException("No hay sesión de cuidador activa")
                val caretakerId = service.fetchCaretakerByUid(caretakerUid)?.id
                    ?: throw IllegalStateException("No encontramos tu perfil de cuidador")

                // id + created_at los rellena la tabla (identity / default now()).
                service.insertVinculation(patientId = patientId, caretakerId = caretakerId)
            }
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("La conexión tardó demasiado, inténtalo de nuevo")
        }
    }

    override suspend fun isCurrentPatientLinked(): Result<Boolean> = runCatching {
        val uid = service.currentUid() ?: return@runCatching false
        withTimeout(NETWORK_TIMEOUT_MS) {
            val patient = service.fetchPatientByUid(uid) ?: return@withTimeout false
            val patientId = patient.id ?: return@withTimeout false
            service.patientHasVinculation(patientId)
        }
    }

    override suspend fun isCurrentCaretakerLinked(): Result<Boolean> = runCatching {
        val uid = service.currentUid() ?: return@runCatching false
        withTimeout(NETWORK_TIMEOUT_MS) {
            val caretaker = service.fetchCaretakerByUid(uid) ?: return@withTimeout false
            val caretakerId = caretaker.id ?: return@withTimeout false
            service.caretakerHasVinculation(caretakerId)
        }
    }

    override suspend fun unlinkCurrent(): Result<Unit> = runCatching {
        val uid = service.currentUid()
            ?: throw IllegalStateException("No hay una sesión activa")

        try {
            withTimeout(NETWORK_TIMEOUT_MS) {
                // El usuario puede ser cuidador o paciente: borramos por el rol que sea.
                service.fetchCaretakerByUid(uid)?.id?.let { caretakerId ->
                    service.deleteVinculationsForCaretaker(caretakerId)
                    return@withTimeout
                }
                service.fetchPatientByUid(uid)?.id?.let { patientId ->
                    service.deleteVinculationsForPatient(patientId)
                    return@withTimeout
                }
                throw IllegalStateException("No encontramos tu perfil para desvincular")
            }
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("La conexión tardó demasiado, inténtalo de nuevo")
        }
    }

    private companion object {
        const val CODE_LENGTH = 6
    }
}

/** Simulación offline: código aleatorio y vínculo confirmado tras un instante. */
class SimulatedLinkRepository(
    private val networkDelayMs: Long = 1_000L,
    private val fixedCode: String = generateLinkCode()
) : LinkRepository {

    override suspend fun getOrCreateMyPatientCode(): Result<String> = runCatching {
        delay(networkDelayMs)
        fixedCode
    }

    override suspend fun linkPatientByCode(code: String): Result<Unit> = runCatching {
        require(code.length == 6 && code.all(Char::isDigit)) {
            "El código debe tener 6 dígitos"
        }
        delay(networkDelayMs)
        Unit
    }

    // Sin backend asumimos que aún no hay vínculo, para mostrar el código.
    override suspend fun isCurrentPatientLinked(): Result<Boolean> = Result.success(false)

    override suspend fun isCurrentCaretakerLinked(): Result<Boolean> = Result.success(false)

    override suspend fun unlinkCurrent(): Result<Unit> = runCatching {
        delay(networkDelayMs)
        Unit
    }
}
