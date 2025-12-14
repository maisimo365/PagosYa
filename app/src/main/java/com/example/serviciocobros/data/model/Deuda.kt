package com.example.serviciocobros.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeudaInsert(
    @SerialName("id_registrador") val idRegistrador: Long,
    @SerialName("id_consumidor") val idConsumidor: Long,
    @SerialName("id_plato") val idPlato: Long? = null,
    val monto: Double,
    @SerialName("saldo_pendiente") val saldoPendiente: Double,
    val descripcion: String? = null,
    @SerialName("precio_plato_en_momento") val precioPlatoMoment: Double
)

@Serializable
data class PlatoSimple(
    @SerialName("nombre_plato") val nombre: String
)

@Serializable
data class Deuda(
    @SerialName("id_deuda") val id: Long,
    val monto: Double,
    @SerialName("saldo_pendiente") val saldoPendiente: Double,
    @SerialName("fecha_consumo") val fecha: String,
    val descripcion: String? = null,
    val platos: PlatoSimple? = null
)