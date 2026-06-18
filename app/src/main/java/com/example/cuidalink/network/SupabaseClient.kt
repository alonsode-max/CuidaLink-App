package com.example.cuidalink.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val SUPABASE_URL = "https://nfcfetejfravhfhaxcvw.supabase.co"

    const val SUPABASE_KEY = "sb_publishable_dWlbzrzUAgx5nlc6-0YjWg_NnEi84ta"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        install(Storage)
        install(Auth)
    }
}
