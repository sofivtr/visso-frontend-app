package cl.duoc.visso.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import cl.duoc.visso.ui.navigation.Screen

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Carrito") },
            selected = currentRoute == "carrito",
            onClick = {
                if (currentRoute != "carrito") {
                    navController.navigate(Screen.Carrito.route)
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Perfil") },
            selected = currentRoute == "perfil",
            onClick = {
                if (currentRoute != "perfil") {
                    navController.navigate(Screen.Perfil.route)
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            label = { Text("Clima") },
            selected = currentRoute == "clima",
            onClick = {
                if (currentRoute != "clima") {
                    navController.navigate(Screen.WeatherScreen.route)
                }
            }
        )
    }
}