package com.example.cuidalink.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.seconds

object SupabaseConfig {
    const val SUPABASE_URL = "https://nfcfetejfravhfhaxcvw.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_dWlbzrzUAgx5nlc6-0YjWg_NnEi84ta"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        // Sin timeout una red colgada deja la UI en carga infinita; lo acotamos.
        requestTimeout = 20.seconds
        install(Storage)
        install(Auth)
        install(Postgrest)
    }
}
