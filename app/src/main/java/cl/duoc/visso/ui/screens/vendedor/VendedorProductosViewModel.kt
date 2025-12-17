package cl.duoc.visso.ui.screens.vendedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.data.repository.ProductoRepository
import cl.duoc.visso.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendedorProductosViewModel @Inject constructor(
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<Resource<List<Producto>>>(Resource.Loading())
    val productos: StateFlow<Resource<List<Producto>>> = _productos

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = Resource.Loading()
            _productos.value = productoRepository.listarProductos()
        }
    }
}