package com.example.cuidalink.repository

import com.example.cuidalink.model.remote.Caretaker
import com.example.cuidalink.model.remote.Patient
import com.example.cuidalink.network.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Alta de cuentas con los campos mínimos del esquema (`caretakers` / `patients`).
 *
 * Devuelve `Result` para manejo explícito de error en la UI.
 */
interface RegistrationRepository {

    /** Registra un cuidador con los 3 campos obligatorios. */
    suspend fun registerCaregiver(name: String, email: String, password: String): Result<Unit>

    /** Registra un paciente con los 4 campos obligatorios. */
    suspend fun registerPatient(
        name: String,
        email: String,
        age: Int,
        password: String
    ): Result<Unit>
}

/** Simulación de red: espera un instante y confirma el alta (offline / sin backend). */
class SimulatedRegistrationRepository(
    private val networkDelayMs: Long = 1_200L
) : RegistrationRepository {

    override suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        delay(networkDelayMs)
        Unit
    }

    override suspend fun registerPatient(
        name: String,
        email: String,
        age: Int,
        password: String
    ): Result<Unit> = runCatching {
        delay(networkDelayMs)
        Unit
    }
}

/**
 * Alta real contra Supabase: crea el usuario en Auth e inserta su fila.
 *
 * Los campos no pedidos en el formulario (grupo sanguíneo, alergias, peso, altura)
 * se insertan vacíos para minimizar fricción; el usuario los completa luego.
 */
class SupabaseRegistrationRepository(
    private val client: SupabaseClient = SupabaseConfig.client
) : RegistrationRepository {

    override suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        val uid = signUp(email, password) {
            put("name", name)
            put("role", "caretaker")
        }
        val caretaker = Caretaker(
            uid = uid,
            name = name,
            email = email,
            createdAt = Instant.now().toString()
        )
        insertIgnoringDuplicate("caretakers", caretaker)
    }

    override suspend fun registerPatient(
        name: String,
        email: String,
        age: Int,
        password: String
    ): Result<Unit> = runCatching {
        val uid = signUp(email, password) {
            put("name", name)
            put("role", "patient")
            put("age", age)
        }
        val patient = Patient(
            uid = uid,
            name = name,
            email = email,
            age = age,
            bloodGroup = "",
            allergies = "",
            weight = 0f,
            height = 0f,
            createdAt = Instant.now().toString()
        )
        insertIgnoringDuplicate("patients", patient)
    }

    private suspend fun signUp(
        email: String,
        password: String,
        metadata: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit
    ): String {
        val user = client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject(metadata)
        }
        return user?.id ?: error("No se obtuvo el id del usuario")
    }

    private suspend inline fun <reified T : Any> insertIgnoringDuplicate(table: String, row: T) {
        try {
            client.from(table).insert(row)
        } catch (e: Exception) {
            if (e.message?.contains("duplicate key", ignoreCase = true) != true) throw e
        }
    }
}
