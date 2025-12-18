package cl.duoc.visso.data.model

import android.os.Parcelable
import cl.duoc.visso.utils.Constants
import cl.duoc.visso.utils.formatPrice
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("codigoProducto") val codigoProducto: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("imagenUrl") val imagenUrl: String?,
    @SerializedName("categoria") val categoria: Categoria,
    @SerializedName("marca") val marca: Marca
) : Parcelable {
    fun getFormattedPrice(): String = precio.formatPrice()
    fun getFullImageUrl(): String = "${Constants.BASE_URL.removeSuffix("/")}${imagenUrl ?: ""}"
    fun esLenteOptico(): Boolean = categoria.nombre.contains("Opticos", ignoreCase = true)
}