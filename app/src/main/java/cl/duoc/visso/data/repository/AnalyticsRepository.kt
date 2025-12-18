package cl.duoc.visso.data.repository

import cl.duoc.visso.data.model.ProductSales
import cl.duoc.visso.data.remote.ApiService
import java.math.BigDecimal

class AnalyticsRepository(
    private val apiService: ApiService
) {

    suspend fun getTopProducts(): List<ProductSales> {
        return apiService.getTopProducts()
    }

    suspend fun getSalesByProduct(): List<ProductSales> {
        return apiService.getSalesByProduct()
    }

    suspend fun getTotalSales(): BigDecimal {
        return apiService.getTotalSales()
    }
}
