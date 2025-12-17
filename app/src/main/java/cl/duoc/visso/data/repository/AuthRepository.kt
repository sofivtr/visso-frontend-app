package cl.duoc.visso.data.repository

import cl.duoc.visso.data.model.*
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun registrar(usuario: Usuario): Resource<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.registrar(usuario)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Error en el registro")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun login(email: String, password: String): Resource<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Credenciales incorrectas")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun recuperarPassword(email: String): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.recuperarPassword(RecuperarPasswordRequest(email))
            if (response.isSuccessful) {
                val mensaje = response.body()?.get("mensaje") ?: "Contraseña restablecida"
                Resource.Success(mensaje)
            } else {
                Resource.Error("El correo no está registrado")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de conexión")
        }
    }
}
