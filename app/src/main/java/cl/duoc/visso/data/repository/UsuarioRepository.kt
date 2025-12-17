package cl.duoc.visso.data.repository

import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun obtenerPerfil(usuarioId: Long): Resource<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerPerfil(usuarioId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar perfil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun actualizarPerfil(usuarioId: Long, usuario: Usuario): Resource<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.actualizarPerfil(usuarioId, usuario)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al actualizar perfil")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun listarUsuarios(): Resource<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listarUsuarios()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al cargar usuarios")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }
    
    suspend fun crearUsuario(usuario: Usuario): Resource<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.crearUsuario(usuario)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al crear usuario"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun eliminarUsuario(usuarioId: Long): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarUsuario(usuarioId)
            if (response.isSuccessful) {
                Resource.Success("Usuario eliminado")
            } else {
                val errorMsg = if (response.code() == 409 || response.code() == 500) {
                    "No se puede eliminar el usuario porque tiene pedidos o carritos asociados"
                } else {
                    "Error al eliminar usuario"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = if (e.message?.contains("constraint", ignoreCase = true) == true ||
                             e.message?.contains("foreign key", ignoreCase = true) == true) {
                "No se puede eliminar el usuario porque tiene pedidos o carritos asociados"
            } else {
                e.localizedMessage ?: "Error de conexión"
            }
            Resource.Error(errorMsg)
        }
    }
}
