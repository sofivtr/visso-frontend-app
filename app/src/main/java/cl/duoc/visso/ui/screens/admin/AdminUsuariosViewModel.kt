package cl.duoc.visso.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.data.repository.AuthRepository
import cl.duoc.visso.data.repository.UsuarioRepository
import cl.duoc.visso.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<Resource<List<Usuario>>>(Resource.Loading())
    val usuarios: StateFlow<Resource<List<Usuario>>> = _usuarios

    private val _operationState = MutableStateFlow<Resource<String>?>(null)
    val operationState: StateFlow<Resource<String>?> = _operationState

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usuarios.value = Resource.Loading()
            _usuarios.value = usuarioRepository.listarUsuarios()
        }
    }

    fun crearUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            val result = usuarioRepository.crearUsuario(usuario)
            _operationState.value = when (result) {
                is Resource.Success -> {
                    cargarUsuarios()
                    Resource.Success("Usuario creado exitosamente")
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Error al crear usuario")
                }
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun actualizarUsuario(id: Long, usuario: Usuario) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            val result = usuarioRepository.actualizarPerfil(id, usuario)
            _operationState.value = when (result) {
                is Resource.Success -> {
                    cargarUsuarios()
                    Resource.Success("Usuario actualizado exitosamente")
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Error al actualizar usuario")
                }
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            val result = usuarioRepository.eliminarUsuario(id)
            _operationState.value = when (result) {
                is Resource.Success -> {
                    cargarUsuarios()
                    Resource.Success("Usuario eliminado exitosamente")
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Error al eliminar usuario")
                }
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }
}