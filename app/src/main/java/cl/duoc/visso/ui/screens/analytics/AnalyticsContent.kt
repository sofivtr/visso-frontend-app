package cl.duoc.visso.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsContent(
    uiState: AnalyticsUiState
) {
    when (uiState) {

        is AnalyticsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AnalyticsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.message)
            }
        }

        is AnalyticsUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // KPI
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Total de Ventas", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$${uiState.totalSales}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                if (uiState.topProducts.isNotEmpty()) {
                    TopProductsChart(uiState.topProducts)
                }

                if (uiState.salesByProduct.isNotEmpty()) {
                    SalesByProductChart(uiState.salesByProduct)
                }
            }
        }
    }
}
