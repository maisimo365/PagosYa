package com.example.serviciocobros.data

import com.example.serviciocobros.BuildConfig
import com.example.serviciocobros.data.model.DeudaInsert
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
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
            val columns = Columns.list(
                "id_usuario",
                "nombre_completo",
                "correo_electronico",
                "es_administrador",
                "empresa",
                "numero_celular",
                "fecha_registro"
            )

            val resultado = client.from("usuarios").select(columns = columns) {
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

    suspend fun obtenerUsuarioPorId(id: Long): Usuario? {
        return try {
            val columns = Columns.list(
                "id_usuario", "nombre_completo", "correo_electronico",
                "es_administrador", "empresa", "numero_celular", "fecha_registro"
            )
            client.from("usuarios").select(columns = columns) {
                filter {
                    eq("id_usuario", id)
                }
            }.decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            println("Error al refrescar usuario: ${e.message}")
            null
        }
    }


    // Funcion para obtener lista de clientes (excluyendo admins)
    suspend fun obtenerClientes(): List<Usuario> {
        return try {
            client.from("usuarios").select {
                filter {
                    eq("activo", true)
                    eq("es_administrador", false)
                }
                order("nombre_completo", Order.ASCENDING)
            }.decodeList<Usuario>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Funcion para obtener lista de platos
    suspend fun obtenerPlatos(): List<Plato> {
        return try {
            client.from("platos").select {
                filter { eq("activo", true) }
                order("nombre_plato", Order.ASCENDING)
            }.decodeList<Plato>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Funcion para guardar la deuda en la BD
    suspend fun registrarDeuda(deuda: DeudaInsert): Boolean {
        return try {
            client.from("deudas").insert(deuda)
            true
        } catch (e: Exception) {
            println("Error Supabase: ${e.message}")
            false
        }
    }
}