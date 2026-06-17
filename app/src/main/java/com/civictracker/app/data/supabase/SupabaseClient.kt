package com.civictracker.app.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://xsdaltpuoayujvphyanp.supabase.co",
        supabaseKey = "sb_publishable_StZKeXj9VkHScSottVytQw_v09_dsFL"
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}