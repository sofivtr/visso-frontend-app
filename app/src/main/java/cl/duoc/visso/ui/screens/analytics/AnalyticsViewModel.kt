package cl.duoc.visso.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.remote.RetrofitClient
import cl.duoc.visso.ui.screens.analytics.AnalyticsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(
        AnalyticsUiState.Loading
    )
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        fetchAnalytics()
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading

            try {
                val topProducts = RetrofitClient.apiService.getTopProducts()
                val salesByProduct = RetrofitClient.apiService.getSalesByProduct()
                val totalSales = RetrofitClient.apiService.getTotalSales()

                _uiState.value = AnalyticsUiState.Success(
                    topProducts = topProducts,
                    salesByProduct = salesByProduct,
                    totalSales = totalSales
                )

            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    e.message ?: "Error al cargar el dashboard analítico"
                )
            }
        }
    }
}