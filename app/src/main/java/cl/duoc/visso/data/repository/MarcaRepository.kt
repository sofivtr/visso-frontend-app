package cl.duoc.visso.data.repository

import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.data.model.Marca
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MarcaRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun obtenerMarcas(): Resource<List<Marca>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.listarMarcas()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Error al obtener marcas: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Error de red: ${e.localizedMessage}")
        }
    }
}
