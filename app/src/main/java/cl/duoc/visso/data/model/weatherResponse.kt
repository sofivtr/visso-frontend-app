package cl.duoc.visso.data.model

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val timezone: String,
    val elevation: Double,
    val current_units: CurrentUnits,
    val current: CurrentWeather
)

// Las unidades para cada dato
data class CurrentUnits(
    val time: String,
    val temperature_2m: String,
    val weather_code: String
)

// Los datos de clima actual
data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val weather_code: Int
)