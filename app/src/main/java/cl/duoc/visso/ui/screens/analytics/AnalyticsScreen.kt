package cl.duoc.visso.ui.screens.analytics

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.visso.ui.screens.analytics.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    onLogout: () -> Unit
) {
    val viewModel: AnalyticsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AnalyticsTopBar(
                onRefresh = { viewModel.fetchAnalytics() },
                onLogout = onLogout
            )
        }
    ) { padding ->
        Surface(
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            AnalyticsContent(uiState = uiState)
        }
    }
}
