package cl.duoc.visso.data.model

import com.google.gson.annotations.SerializedName

data class Cotizacion(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("usuario") val usuario: Usuario? = null,
    @SerializedName("producto") val producto: Producto? = null,
    @SerializedName("nombrePaciente") val nombrePaciente: String,
    @SerializedName("fechaReceta") val fechaReceta: String,
    @SerializedName("gradoOd") val gradoOd: Double,
    @SerializedName("gradoOi") val gradoOi: Double,
    @SerializedName("tipoLente") val tipoLente: String,
    @SerializedName("tipoCristal") val tipoCristal: String,
    @SerializedName("antirreflejo") val antirreflejo: Boolean,
    @SerializedName("filtroAzul") val filtroAzul: Boolean,
    @SerializedName("despachoDomicilio") val despachoDomicilio: Boolean,
    @SerializedName("valorAprox") val valorAprox: Double? = null
)