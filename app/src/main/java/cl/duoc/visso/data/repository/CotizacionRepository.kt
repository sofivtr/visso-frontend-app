package cl.duoc.visso.data.repository

import cl.duoc.visso.data.model.Cotizacion
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CotizacionRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun crearCotizacion(cotizacion: Cotizacion): Resource<Cotizacion> = withContext(Dispatchers.IO) {
        try {
            val response = api.crearCotizacion(cotizacion)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al crear cotización")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun listarCotizacionesPorUsuario(usuarioId: Long): Resource<List<Cotizacion>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listarCotizacionesPorUsuario(usuarioId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar cotizaciones")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }
}