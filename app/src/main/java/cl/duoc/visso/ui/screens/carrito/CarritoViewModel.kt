package cl.duoc.visso.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Carrito
import cl.duoc.visso.data.repository.CarritoRepository
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarritoViewModel @Inject constructor(
    private val repository: CarritoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _carrito = MutableStateFlow<Resource<Carrito>>(Resource.Loading())
    val carrito: StateFlow<Resource<Carrito>> = _carrito

    private val _checkoutState = MutableStateFlow<Resource<String>?>(null)
    val checkoutState: StateFlow<Resource<String>?> = _checkoutState

    private val _deleteState = MutableStateFlow<Resource<String>?>(null)
    val deleteState: StateFlow<Resource<String>?> = _deleteState

    init {
        loadCarrito()
    }

    fun loadCarrito() {
        viewModelScope.launch {
            _carrito.value = Resource.Loading()
            val userId = sessionManager.userId.first()
            _carrito.value = repository.obtenerCarrito(userId)
        }
    }

    fun finalizarCompra() {
        viewModelScope.launch {
            _checkoutState.value = Resource.Loading()
            val userId = sessionManager.userId.first()
            _checkoutState.value = repository.cerrarCarrito(userId)
        }
    }

    fun eliminarDelCarrito(detalleId: Long) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            _deleteState.value = repository.eliminarDelCarrito(detalleId)

            // Recargar el carrito después de eliminar
            if (_deleteState.value is Resource.Success) {
                loadCarrito()
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = null
    }

    fun resetDeleteState() {
        _deleteState.value = null
    }
}