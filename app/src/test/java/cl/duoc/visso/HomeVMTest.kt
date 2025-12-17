package cl.duoc.visso
import cl.duoc.visso.data.model.Categoria
import cl.duoc.visso.data.model.Marca
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.data.repository.CarritoRepository
import cl.duoc.visso.data.repository.ProductoRepository
import cl.duoc.visso.ui.screens.home.HomeViewModel
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BehaviorSpec({

    val productoRepository = mockk<ProductoRepository>()
    val carritoRepository = mockk<CarritoRepository>()
    val sessionManager = mockk<SessionManager>(relaxed = true)

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("Un HomeViewModel") {

        val categoriaDummy = Categoria(id = 1, nombre = "Ópticos")
        val marcaDummy = Marca(id = 1, nombre = "Ray-Ban")

        val productoPrueba = Producto(
            id = 1L,
            codigoProducto = "OPT-001",
            nombre = "Lente de Prueba",
            descripcion = "Descripción del lente",
            precio = 50000.0,
            stock = 10,
            fechaCreacion = "2025-01-01",
            imagenUrl = "/img/lente.jpg",
            categoria = categoriaDummy,
            marca = marcaDummy
        )

        coEvery { productoRepository.listarProductos() } returns Resource.Success(listOf(productoPrueba))
        coEvery { productoRepository.listarCategorias() } returns Resource.Success(listOf(categoriaDummy))

        When("El ViewModel se inicializa") {
            val viewModel = HomeViewModel(productoRepository, carritoRepository, sessionManager)

            testDispatcher.scheduler.advanceUntilIdle()

            Then("Debe cargar la lista de productos correctamente") {
                val estadoActual = viewModel.productos.value

                estadoActual.shouldBeInstanceOf<Resource.Success<List<Producto>>>()

                val listaProductos = estadoActual.data
                listaProductos?.size shouldBe 1
                listaProductos?.first()?.nombre shouldBe "Lente de Prueba"
                listaProductos?.first()?.codigoProducto shouldBe "OPT-001"
            }
        }
    }
})