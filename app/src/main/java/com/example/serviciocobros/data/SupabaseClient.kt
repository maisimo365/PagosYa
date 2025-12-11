package com.example.serviciocobros.data

import com.example.serviciocobros.BuildConfig
import com.example.serviciocobros.data.model.Usuario
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    //Conexion con supabase
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Realtime)
    }

    // Función para validar el login
    suspend fun login(correo: String, pass: String): Usuario? {
        try {
            val resultado = client.from("usuarios").select(columns = Columns.list("id_usuario", "nombre_completo", "correo_electronico", "es_administrador", "empresa")) {
                filter {
                    eq("correo_electronico", correo)
                    eq("contrasena", pass)
                    eq("activo", true)
                }
            }.decodeSingleOrNull<Usuario>()

            return resultado
        } catch (e: Exception) {
            println("Error en login: ${e.message}")
            return null
        }
    }
}