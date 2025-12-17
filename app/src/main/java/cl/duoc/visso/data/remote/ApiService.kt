// ========================================
// data/remote/ApiService.kt - VERSIÓN COMPLETA
// ========================================
package cl.duoc.visso.data.remote

import cl.duoc.visso.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ===== AUTENTICACIÓN =====
    @POST("api/auth/registro")
    suspend fun registrar(@Body usuario: Usuario): Response<Usuario>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Usuario>

    @POST("api/auth/recuperar-password")
    suspend fun recuperarPassword(@Body request: RecuperarPasswordRequest): Response<Map<String, String>>

    // ===== PRODUCTOS =====
    @GET("api/productos")
    suspend fun listarProductos(): Response<List<Producto>>

    @GET("api/productos/{id}")
    suspend fun obtenerProducto(@Path("id") id: Long): Response<Producto>

    @POST("api/productos")
    suspend fun crearProducto(@Body producto: Producto): Response<Producto>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(@Path("id") id: Long, @Body producto: Producto): Response<Producto>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Long): Response<Unit>

    // ===== IMÁGENES =====
    @Multipart
    @POST("api/imagenes/upload")
    suspend fun subirImagen(
        @Part imagen: okhttp3.MultipartBody.Part,
        @Part("categoria") categoria: okhttp3.RequestBody
    ): Response<ImagenResponse>

    // ===== CATEGORÍAS =====
    @GET("api/categorias")
    suspend fun listarCategorias(): Response<List<Categoria>>

    // ===== MARCAS =====
    @GET("api/marcas")
    suspend fun listarMarcas(): Response<List<Marca>>

    // ===== CARRITO =====
    @GET("api/carrito/{usuarioId}")
    suspend fun obtenerCarrito(@Path("usuarioId") usuarioId: Long): Response<Carrito>

    @POST("api/carrito/agregar")
    suspend fun agregarAlCarrito(@Body solicitud: SolicitudCarrito): Response<Unit>

    @POST("api/carrito/cerrar/{usuarioId}")
    suspend fun cerrarCarrito(@Path("usuarioId") usuarioId: Long): Response<Unit>

    @DELETE("api/carrito/detalle/{detalleId}")
    suspend fun eliminarDelCarrito(@Path("detalleId") detalleId: Long): Response<Unit>

    @GET("api/carrito/detalle/{detalleId}")
    suspend fun obtenerDetalleCarrito(@Path("detalleId") detalleId: Long): Response<DetalleCarrito>

    // ===== COTIZACIONES =====
    @POST("api/cotizaciones")
    suspend fun crearCotizacion(@Body cotizacion: Cotizacion): Response<Cotizacion>

    @GET("api/cotizaciones/usuario/{usuarioId}")
    suspend fun listarCotizacionesPorUsuario(@Path("usuarioId") usuarioId: Long): Response<List<Cotizacion>>

    // ===== USUARIOS =====
    @GET("api/usuarios/{id}")
    suspend fun obtenerPerfil(@Path("id") id: Long): Response<Usuario>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Long, @Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<Usuario>>

    @POST("api/usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<Usuario>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Long): Response<Unit>

    // ===== ADMIN - VENTAS =====
    @GET("api/carrito/ventas")
    suspend fun listarVentas(): Response<List<Carrito>>

    // ===== API CLIMA =====
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}