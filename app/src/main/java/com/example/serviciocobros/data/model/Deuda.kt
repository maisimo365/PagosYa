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