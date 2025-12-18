package cl.duoc.visso.ui.screens.cotizacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Cotizacion
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.data.model.SolicitudCarrito
import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.data.repository.CarritoRepository
import cl.duoc.visso.data.repository.CotizacionRepository
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CotizacionViewModel @Inject constructor(
    private val cotizacionRepository: CotizacionRepository,
    private val carritoRepository: CarritoRepository,
    private val productoRepository: cl.duoc.visso.data.repository.ProductoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _productoState = MutableStateFlow<Resource<Producto>>(Resource.Loading())
    val productoState: StateFlow<Resource<Producto>> = _productoState
    
    private val _cotizacionState = MutableStateFlow<Resource<Cotizacion>?>(null)
    val cotizacionState: StateFlow<Resource<Cotizacion>?> = _cotizacionState
    
    private val _agregarState = MutableStateFlow<Resource<Unit>?>(null)
    val agregarState: StateFlow<Resource<Unit>?> = _agregarState
    
    fun cargarProducto(productoId: Long) {
        viewModelScope.launch {
            _productoState.value = Resource.Loading()
            _productoState.value = productoRepository.obtenerProductoPorId(productoId)
        }
    }

    fun crearCotizacionYAgregarCarrito(
        producto: Producto,
        nombrePaciente: String,
        fechaReceta: String,
        gradoOd: Double,
        gradoOi: Double,
        tipoLente: String,
        tipoCristal: String,
        antirreflejo: Boolean,
        filtroAzul: Boolean,
        despachoDomicilio: Boolean
    ) {
        viewModelScope.launch {
            _cotizacionState.value = Resource.Loading()

            val userId = sessionManager.userId.first()

            // Crear la cotización
            val cotizacion = Cotizacion(
                usuario = Usuario(id = userId, nombre = "", apellido = "", rut = "", email = ""),
                producto = producto,
                nombrePaciente = nombrePaciente,
                fechaReceta = fechaReceta,
                gradoOd = gradoOd,
                gradoOi = gradoOi,
                tipoLente = tipoLente,
                tipoCristal = tipoCristal,
                antirreflejo = antirreflejo,
                filtroAzul = filtroAzul,
                despachoDomicilio = despachoDomicilio
            )

            val result = cotizacionRepository.crearCotizacion(cotizacion)

            when (result) {
                is Resource.Success -> {
                    _cotizacionState.value = result

                    // Ahora agregar al carrito con la cotización
                    _agregarState.value = Resource.Loading()
                    val solicitud = SolicitudCarrito(
                        usuarioId = userId,
                        productoId = producto.id ?: 0,
                        cantidad = 1,
                        cotizacionId = result.data?.id
                    )

                    val agregarResult = carritoRepository.agregarProducto(solicitud)
                    _agregarState.value = when (agregarResult) {
                        is Resource.Success -> Resource.Success(Unit)
                        is Resource.Error -> Resource.Error(agregarResult.message ?: "Error al agregar")
                        is Resource.Loading -> Resource.Loading()
                    }
                }
                is Resource.Error -> {
                    _cotizacionState.value = result
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _cotizacionState.value = null
        _agregarState.value = null
    }
}