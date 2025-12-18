package cl.duoc.visso.utils

import java.text.NumberFormat
import java.util.*

fun Double.formatPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return format.format(this).replace("CLP", "$").trim()
}