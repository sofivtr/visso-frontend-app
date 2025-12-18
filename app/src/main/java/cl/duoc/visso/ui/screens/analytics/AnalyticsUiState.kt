package cl.duoc.visso.ui.screens.analytics

import cl.duoc.visso.data.model.ProductSales
import java.math.BigDecimal

sealed class AnalyticsUiState {

    object Loading : AnalyticsUiState()

    data class Success(
        val topProducts: List<ProductSales>,
        val salesByProduct: List<ProductSales>,
        val totalSales: BigDecimal
    ) : AnalyticsUiState()

    data class Error(
        val message: String
    ) : AnalyticsUiState()
}
