package com.example.serviciocobros.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Serializable
@Parcelize
data class Usuario(
    @SerialName("id_usuario") val id: Long,
    @SerialName("nombre_completo") val nombre: String,
    @SerialName("correo_electronico") val correo: String,
    @SerialName("es_administrador") val esAdmin: Boolean = false,
    @SerialName("numero_celular") val celular: String? = null,
    @SerialName("fecha_registro") val fechaRegistro: String? = null,
    val empresa: String? = null,
    val activo: Boolean = true
) : Parcelable

@Serializable
data class UsuarioInsert(
    @SerialName("nombre_completo") val nombre: String,
    @SerialName("correo_electronico") val correo: String,
    @SerialName("contrasena") val contrasena: String,
    @SerialName("es_administrador") val esAdmin: Boolean = false,
    @SerialName("numero_celular") val celular: String? = null,
    val activo: Boolean = true,
    val empresa: String? = null
)