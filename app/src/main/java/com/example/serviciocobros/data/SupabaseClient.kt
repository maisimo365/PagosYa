package com.example.serviciocobros.data

import com.example.serviciocobros.BuildConfig
import com.example.serviciocobros.data.model.Deuda
import com.example.serviciocobros.data.model.DeudaInsert
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.Realtime
import com.example.serviciocobros.data.model.PagoInsert
import kotlin.math.min
import com.example.serviciocobros.data.model.PagoHistorico
import com.example.serviciocobros.data.model.PlatoInsert
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import com.example.serviciocobros.data.model.UsuarioInsert

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
        install(Storage)
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

    // Funcion para obtener la deuda de la BD
    suspend fun obtenerMisDeudas(idUsuario: Long): List<Deuda> {
        return try {
            val columns = Columns.raw("*, platos(nombre_plato)")
            client.from("deudas").select(columns = columns) {
                filter {
                    eq("id_consumidor", idUsuario)
                    eq("activo", true)
                    gt("saldo_pendiente", 0)
                }
                order("fecha_consumo", Order.DESCENDING)
            }.decodeList<Deuda>()
        } catch (e: Exception) {
            println("Error al obtener deudas: ${e.message}")
            emptyList()
        }
    }

    // Funcion para registrar pago en la BD
    suspend fun registrarPago(idConsumidor: Long, idCobrador: Long, montoTotal: Double): Boolean {
        return try {
            val deudasActivas = client.from("deudas").select {
                filter {
                    eq("id_consumidor", idConsumidor)
                    gt("saldo_pendiente", 0)
                }
                order("fecha_consumo", Order.ASCENDING)
            }.decodeList<Deuda>()

            var montoRestante = montoTotal

            for (deuda in deudasActivas) {
                if (montoRestante <= 0) break
                val montoAPagar = min(deuda.saldoPendiente, montoRestante)
                val nuevoSaldo = deuda.saldoPendiente - montoAPagar
                val tipoPago = if (nuevoSaldo <= 0.0) "completo" else "parcial"
                val pago = PagoInsert(
                    idConsumidor = idConsumidor,
                    idCobrador = idCobrador,
                    idDeuda = deuda.id,
                    montoPagado = montoAPagar,
                    tipoPago = tipoPago
                )
                client.from("pagos").insert(pago)

                client.from("deudas").update({
                    set("saldo_pendiente", nuevoSaldo)

                }) {
                    filter { eq("id_deuda", deuda.id) }
                }

                montoRestante -= montoAPagar
            }
            true
        } catch (e: Exception) {
            println("Error al registrar pago: ${e.message}")
            false
        }
    }
    // Funcion para obtener historial de pagos de la BD
    suspend fun obtenerHistorialPagos(idUsuario: Long): List<PagoHistorico> {
        return try {
            val columns = Columns.raw("*, deudas(fecha_consumo, platos(nombre_plato, foto_plato))")

            client.from("pagos").select(columns = columns) {
                filter {
                    eq("id_consumidor", idUsuario)
                }
                order("fecha_pago", Order.DESCENDING)
            }.decodeList<PagoHistorico>()
        } catch (e: Exception) {
            println("Error historial: ${e.message}")
            emptyList()
        }
    }

    // Funcion para crear platos
    suspend fun crearPlato(plato: PlatoInsert): Boolean {
        return try {
            client.from("platos").insert(plato)
            true
        } catch (e: Exception) {
            println("Error al crear plato: ${e.message}")
            false
        }
    }
    // Funcion para crear platos y guardar en la BD
    suspend fun subirImagenPlato(byteArray: ByteArray): String? {
        return try {
            val bucket = client.storage.from("platos")
            val fileName = "plato_${System.currentTimeMillis()}.jpg"

            bucket.upload(fileName, byteArray) {
                upsert = false
            }

            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            println("Error subiendo imagen: ${e.message}")
            null
        }
    }
    // Funcion para desactivar (borrar logicamente) un plato
    suspend fun eliminarPlato(idPlato: Long): Boolean {
        return try {
            client.from("platos").update({
                set("activo", false)
            }) {
                filter {
                    eq("id_plato", idPlato)
                }
            }
            true
        } catch (e: Exception) {
            println("Error al eliminar plato: ${e.message}")
            false
        }
    }

    // Función para actualizar un plato existente
    suspend fun actualizarPlato(id: Long, nombre: String, precio: Double, fotoUrl: String?): Boolean {
        return try {
            client.from("platos").update({
                set("nombre_plato", nombre)
                set("precio", precio)
                if (fotoUrl != null) {
                    set("foto_plato", fotoUrl)
                }
            }) {
                filter {
                    eq("id_plato", id)
                }
            }
            true
        } catch (e: Exception) {
            println("Error al actualizar plato: ${e.message}")
            false
        }
    }

    // Función para crear un nuevo usuario
    suspend fun crearUsuario(usuario: UsuarioInsert): Boolean {
        return try {
            client.from("usuarios").insert(usuario)
            true
        } catch (e: Exception) {
            println("Error al crear usuario: ${e.message}")
            false
        }
    }

}