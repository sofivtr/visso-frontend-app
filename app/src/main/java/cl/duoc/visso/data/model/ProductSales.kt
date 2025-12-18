package cl.duoc.visso.data.model

import java.math.BigDecimal

//USO SOLO PARA EL DASHBOARD
data class ProductSales(
    val productName: String,
    val quantity: Long,
    val totalSales: BigDecimal
)