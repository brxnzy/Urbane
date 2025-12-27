package com.example.urbane.data.remote

import com.example.urbane.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import kotlin.time.Duration.Companion.seconds

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
    install(Functions)
    install(Realtime){
        reconnectDelay = 5.seconds
    }
    httpEngine = CIO.create {
        endpoint {
            connectTimeout = 15_000
            requestTimeout = 30_000
        }
    }
}
