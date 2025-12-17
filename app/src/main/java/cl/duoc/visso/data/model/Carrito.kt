package cl.duoc.visso.data.model

import cl.duoc.visso.utils.formatPrice
import com.google.gson.annotations.SerializedName

data class Carrito(
    @SerializedName("id") val id: Long,
    @SerializedName("usuario") val usuario: Usuario,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("total") val total: Double,
    @SerializedName("detalles") val detalles: List<DetalleCarrito> = emptyList()
) {
    fun getFormattedTotal(): String = total.formatPrice()
}