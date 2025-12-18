package cl.duoc.visso.ui.screens.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTopBar(
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = { Text("Dashboard Analítico") },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
            }
        }
    )
}
