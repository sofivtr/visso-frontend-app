package cl.duoc.visso.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RecuperarPasswordRequest(
    @SerializedName("email") val email: String
)

data class SolicitudCarrito(
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("productoId") val productoId: Long,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("cotizacionId") val cotizacionId: Long? = null
)

data class ImagenResponse(
    @SerializedName("imagenUrl") val imagenUrl: String,
    @SerializedName("mensaje") val mensaje: String
)