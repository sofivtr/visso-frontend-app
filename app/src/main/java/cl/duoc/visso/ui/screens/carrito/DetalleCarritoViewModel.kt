package cl.duoc.visso.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.DetalleCarrito
import cl.duoc.visso.data.repository.CarritoRepository
import cl.duoc.visso.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleCarritoViewModel @Inject constructor(
    private val carritoRepository: CarritoRepository
) : ViewModel() {
    
    private val _detalleState = MutableStateFlow<Resource<DetalleCarrito>>(Resource.Loading())
    val detalleState: StateFlow<Resource<DetalleCarrito>> = _detalleState
    
    fun cargarDetalle(detalleId: Long) {
        viewModelScope.launch {
            _detalleState.value = Resource.Loading()
            _detalleState.value = carritoRepository.obtenerDetalleCarrito(detalleId)
        }
    }
}
