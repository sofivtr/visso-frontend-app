package cl.duoc.visso.ui.screens.admin

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
class AdminViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _ventas = MutableStateFlow<Resource<List<Carrito>>>(Resource.Loading())
    val ventas: StateFlow<Resource<List<Carrito>>> = _ventas

    init {
        cargarVentas()
    }

    fun cargarVentas() {
        viewModelScope.launch {
            _ventas.value = Resource.Loading()
            try {
                val response = apiService.listarVentas()
                if (response.isSuccessful && response.body() != null) {
                    _ventas.value = Resource.Success(response.body()!!)
                } else {
                    _ventas.value = Resource.Error("Error al cargar ventas")
                }
            } catch (e: Exception) {
                _ventas.value = Resource.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }
}