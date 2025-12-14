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
@Serializable
data class PagoInsert(
    @SerialName("id_consumidor") val idConsumidor: Long,
    @SerialName("id_cobrador") val idCobrador: Long,
    @SerialName("id_deuda") val idDeuda: Long,
    @SerialName("monto_pagado") val montoPagado: Double,
    @SerialName("tipo_pago") val tipoPago: String, // 'completo' o 'parcial'
    @SerialName("metodo_pago") val metodoPago: String = "efectivo"
)

@Serializable
data class PlatoConFoto(
    @SerialName("nombre_plato") val nombre: String,
    @SerialName("foto_plato") val foto: String? = null
)

@Serializable
data class DeudaDetalle(
    @SerialName("fecha_consumo") val fechaConsumo: String,
    val platos: PlatoConFoto? = null
)

@Serializable
data class PagoHistorico(
    @SerialName("id_pago") val id: Long,
    @SerialName("monto_pagado") val montoPagado: Double,
    @SerialName("fecha_pago") val fechaPago: String,
    val deudas: DeudaDetalle? = null // Relación anidada
)