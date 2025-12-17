package cl.duoc.visso.data.repository

import cl.duoc.visso.data.model.*
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun obtenerCarrito(usuarioId: Long): Resource<Carrito> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerCarrito(usuarioId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar carrito")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun agregarProducto(solicitud: SolicitudCarrito): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.agregarAlCarrito(solicitud)
            if (response.isSuccessful) {
                Resource.Success("Producto agregado al carrito")
            } else {
                Resource.Error("Error al agregar producto")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun cerrarCarrito(usuarioId: Long): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.cerrarCarrito(usuarioId)
            if (response.isSuccessful) {
                Resource.Success("Compra realizada con éxito")
            } else {
                Resource.Error("Error al finalizar compra")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun eliminarDelCarrito(detalleId: Long): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarDelCarrito(detalleId)
            if (response.isSuccessful) {
                Resource.Success("Producto eliminado del carrito")
            } else {
                Resource.Error("Error al eliminar producto")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun obtenerDetalleCarrito(detalleId: Long): Resource<DetalleCarrito> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerDetalleCarrito(detalleId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar detalle")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }
}