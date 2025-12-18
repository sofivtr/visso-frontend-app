package cl.duoc.visso.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.visso.ui.screens.auth.*
import cl.duoc.visso.ui.screens.home.HomeScreen
import cl.duoc.visso.ui.screens.carrito.CarritoScreen
import cl.duoc.visso.ui.screens.carrito.DetalleCarritoScreen
import cl.duoc.visso.ui.screens.perfil.PerfilScreen
import cl.duoc.visso.ui.screens.cotizacion.CotizacionScreen
import cl.duoc.visso.ui.screens.admin.AdminHomeScreen
import cl.duoc.visso.ui.screens.admin.AdminProductosScreen
import cl.duoc.visso.ui.screens.admin.AdminUsuariosScreen
import cl.duoc.visso.ui.screens.analytics.AnalyticsScreen
import cl.duoc.visso.ui.screens.weather.WeatherScreen
import cl.duoc.visso.ui.screens.vendedor.VendedorHomeScreen
import cl.duoc.visso.ui.screens.vendedor.VendedorProductosScreen


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ===== AUTH FLOW =====
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { rol ->
                    // Redirigir según el rol del usuario
                    val destination = when (rol) {
                        "ADMIN" -> Screen.AdminHome.route
                        "VENDEDOR" -> Screen.VendedorHome.route
                        "ANALYST" -> Screen.DashboardAnalista.route
                        else -> Screen.Home.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== USER FLOW =====
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.Carrito.route) {
            CarritoScreen(navController)
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(navController)
        }

        // ===== COTIZACIÓN =====
        composable(
            route = "cotizacion/{productoId}",
            arguments = listOf(navArgument("productoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getLong("productoId") ?: 0L
            CotizacionScreen(
                productoId = productoId,
                onNavigateBack = { navController.popBackStack() },
                onCotizacionExitosa = {
                    navController.popBackStack()
                    navController.navigate(Screen.Carrito.route)
                }
            )
        }

        // ===== DETALLE CARRITO =====
        composable(
            route = "detalle_carrito/{detalleId}",
            arguments = listOf(navArgument("detalleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val detalleId = backStackEntry.arguments?.getLong("detalleId") ?: 0L
            DetalleCarritoScreen(
                detalleCarritoId = detalleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== ADMIN FLOW =====
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController)
        }

        composable(Screen.AdminProductos.route) {
            AdminProductosScreen(navController)
        }

        composable(Screen.AdminUsuarios.route) {
            AdminUsuariosScreen(navController)
        }

        composable(Screen.WeatherScreen.route) {
            WeatherScreen(navController)
        }


        // ===== VENDEDOR FLOW =====
        composable(Screen.VendedorHome.route) {
            VendedorHomeScreen(navController)
        }

        composable(Screen.VendedorProductos.route) {
            VendedorProductosScreen(navController)
        }

        composable(Screen.DashboardAnalista.route) {
            AnalyticsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

}