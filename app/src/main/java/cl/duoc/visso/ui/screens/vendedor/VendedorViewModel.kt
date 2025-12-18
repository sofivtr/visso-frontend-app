package cl.duoc.visso.ui.screens.vendedor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Carrito
import cl.duoc.visso.data.remote.ApiService
import cl.duoc.visso.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendedorViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _pedidos = MutableStateFlow<Resource<List<Carrito>>>(Resource.Loading())
    val pedidos: StateFlow<Resource<List<Carrito>>> = _pedidos

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            _pedidos.value = Resource.Loading()
            try {
                val response = apiService.listarVentas()
                if (response.isSuccessful && response.body() != null) {
                    _pedidos.value = Resource.Success(response.body()!!)
                } else {
                    _pedidos.value = Resource.Error("Error al cargar pedidos")
                }
            } catch (e: Exception) {
                _pedidos.value = Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }
}