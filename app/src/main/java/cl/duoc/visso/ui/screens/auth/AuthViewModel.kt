package cl.duoc.visso.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.data.repository.AuthRepository
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Usuario>?>(null)
    val authState: StateFlow<Resource<Usuario>?> = _authState

    private val _forgotPasswordState = MutableStateFlow<Resource<String>?>(null)
    val forgotPasswordState: StateFlow<Resource<String>?> = _forgotPasswordState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.login(email, password)
            if (result is Resource.Success) {
                result.data?.let { sessionManager.saveUserSession(it) }
            }
            _authState.value = result
        }
    }

    fun registrar(usuario: Usuario) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.registrar(usuario)
            if (result is Resource.Success) {
                result.data?.let { sessionManager.saveUserSession(it) }
            }
            _authState.value = result
        }
    }

    fun recuperarPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = Resource.Loading()
            _forgotPasswordState.value = repository.recuperarPassword(email)
        }
    }

    fun resetAuthState() {
        _authState.value = null
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = null
    }
}