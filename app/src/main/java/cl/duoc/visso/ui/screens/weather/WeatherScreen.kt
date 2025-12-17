package cl.duoc.visso.ui.screens.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.visso.data.model.WeatherResponse
import cl.duoc.visso.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = viewModel()
) {
    val uiState by weatherViewModel.weatherData.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is WeatherViewModel.UiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando datos del clima...", color = Color.Gray)
                    }
                }
                is WeatherViewModel.UiState.Success -> {
                    val data = (uiState as WeatherViewModel.UiState.Success).response
                    WeatherDataDisplay(data)
                }
                is WeatherViewModel.UiState.Error -> {
                    val message = (uiState as WeatherViewModel.UiState.Error).message
                    ErrorDisplay(message)
                }
            }
        }
    }
}

@Composable
fun WeatherDataDisplay(data: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Clima en ${data.timezone}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
        Spacer(Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Temperatura Actual",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${data.current.temperature_2m}${data.current_units.temperature_2m}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2196F3)
                )

                Image(
                    painter = painterResource(id = R.drawable.icon_weather_default),
                    contentDescription = "Icono de clima",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(top = 16.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)
        DataRow(label = "Elevación", value = "${data.elevation}m")
        DataRow(label = "Latitud", value = "${data.latitude}°")
        DataRow(label = "Longitud", value = "${data.longitude}°")
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        Text(text = value, color = Color.Gray)
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(60.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Error al conectar con el clima:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}