package cl.duoc.visso.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Carrito : Screen("carrito")
    object Perfil : Screen("perfil")
    object Cotizacion : Screen("cotizacion") // NUEVO

    // Admin routes
    object AdminHome : Screen("admin/home")
    object AdminProductos : Screen("admin/productos")
    object AdminUsuarios : Screen("admin/usuarios")
    object WeatherScreen : Screen("weather")
}