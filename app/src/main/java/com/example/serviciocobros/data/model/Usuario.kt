package com.example.serviciocobros.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id_usuario") val id: Long,
    @SerialName("nombre_completo") val nombre: String,
    @SerialName("correo_electronico") val correo: String,
    @SerialName("es_administrador") val esAdmin: Boolean = false,
    val empresa: String? = null
)