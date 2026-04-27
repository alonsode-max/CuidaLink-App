package com.example.cuidalink.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val SUPABASE_URL = "https://bcsijmharhovfebecqxx.supabase.co"

    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJjc2lqbWhhcmhvdmZlYmVjcXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcyNjg3NzQsImV4cCI6MjA5Mjg0NDc3NH0.4ZyJx9Bch66hPTgOghR4vHWM3DIDbh-7551wJProJfY"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        install(Storage)
        install(Auth)
    }
}
