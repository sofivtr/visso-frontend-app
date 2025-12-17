package cl.duoc.visso.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Marca(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String
) : Parcelable