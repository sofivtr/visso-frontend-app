package cl.duoc.visso.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.WeatherResponse
import cl.duoc.visso.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<UiState>(UiState.Loading)
    val weatherData: StateFlow<UiState> = _weatherData

    sealed class UiState {
        object Loading : UiState()
        data class Success(val response: WeatherResponse) : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        fetchWeatherData(lat = -33.44, lon = -70.65)
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherData.value = UiState.Loading

            try {
                val response = RetrofitClient.extApiService.getCurrentWeather(lat, lon)
                _weatherData.value = UiState.Success(response)

            } catch (e: Exception) {
                _weatherData.value = UiState.Error(e.message ?: "Error de red o timeout")
            }
        }
    }
}