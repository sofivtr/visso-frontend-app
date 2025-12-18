package cl.duoc.visso.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("rut") val rut: String,
    @SerializedName("email") val email: String,
    @SerializedName("passwordHash") val passwordHash: String? = null,
    @SerializedName("rol") val rol: String? = "USER",
    @SerializedName("fechaRegistro") val fechaRegistro: String? = null,
    @SerializedName("activo") val activo: Boolean? = true
)