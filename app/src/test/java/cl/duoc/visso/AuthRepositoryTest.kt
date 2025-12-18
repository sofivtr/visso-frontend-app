package cl.duoc.visso

import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.data.repository.AuthRepository
import cl.duoc.visso.utils.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class AuthRepositoryTest {

    private val apiService = mockk<ApiService>()

    private lateinit var authRepository: AuthRepository

    @BeforeEach
    fun setup() {
        authRepository = AuthRepository(apiService)
    }

    @Test
    fun `login debe retornar Success cuando la API responde correctamente`() = runTest {
        val email = "test@duoc.cl"
        val password = "123"
        val usuarioEsperado =
            Usuario(nombre = "Test", apellido = "User", rut = "1-9", email = email)

        // Simulamos que la API responde exitosamente (coEvery para suspend functions)
        coEvery { apiService.login(any()) } returns Response.success(usuarioEsperado)

        // WHEN: Ejecutamos la acción
        val resultado = authRepository.login(email, password)

        assertTrue(resultado is Resource.Success)
        assertEquals(usuarioEsperado, (resultado as Resource.Success).data)

        coVerify(exactly = 1) { apiService.login(match { it.email == email }) }
    }
}