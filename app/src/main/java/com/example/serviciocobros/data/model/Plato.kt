package com.example.serviciocobros.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plato(
    @SerialName("id_plato") val id: Long,
    @SerialName("nombre_plato") val nombre: String,
    val precio: Double,
    @SerialName("foto_plato") val fotoUrl: String? = null,
)