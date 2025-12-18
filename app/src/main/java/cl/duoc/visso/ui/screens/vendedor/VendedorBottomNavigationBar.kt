package cl.duoc.visso.ui.screens.vendedor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import cl.duoc.visso.ui.theme.BluePrimary

@Composable
fun VendedorBottomNavigationBar(
    navController: NavController,
    currentRoute: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, "Pedidos") },
            label = { Text("Pedidos") },
            selected = currentRoute == "vendedor/home",
            onClick = {
                if (currentRoute != "vendedor/home") {
                    navController.navigate("vendedor/home") {
                        popUpTo("vendedor/home") { inclusive = true }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = BluePrimary.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Inventory, "Productos") },
            label = { Text("Productos") },
            selected = currentRoute == "vendedor/productos",
            onClick = {
                if (currentRoute != "vendedor/productos") {
                    navController.navigate("vendedor/productos") {
                        popUpTo("vendedor/home") { inclusive = false }
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = BluePrimary.copy(alpha = 0.1f)
            )
        )
    }
}