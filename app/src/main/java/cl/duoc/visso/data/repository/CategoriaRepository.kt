package cl.duoc.visso.data.repository

import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.data.model.Categoria
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoriaRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun obtenerCategorias(): Resource<List<Categoria>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.listarCategorias()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Error al obtener categorías: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Error de red: ${e.localizedMessage}")
        }
    }
}
