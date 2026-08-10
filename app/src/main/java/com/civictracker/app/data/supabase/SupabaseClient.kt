package com.civictracker.app.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import com.civictracker.app.BuildConfig

object SupabaseClient {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false // Important: Don't send nulls/defaults to avoid schema errors
        coerceInputValues = true
    }

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
        
        defaultSerializer = KotlinXSerializer(json)
    }
}
