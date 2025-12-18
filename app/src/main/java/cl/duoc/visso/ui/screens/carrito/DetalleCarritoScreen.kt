package cl.duoc.visso.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCarritoScreen(
    detalleCarritoId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DetalleCarritoViewModel = hiltViewModel()
) {
    val detalleState by viewModel.detalleState.collectAsState()
    
    LaunchedEffect(detalleCarritoId) {
        viewModel.cargarDetalle(detalleCarritoId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cotización") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when (val state = detalleState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is Resource.Success -> {
                state.data?.let { detalle ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Información del Producto
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Producto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = detalle.nombreProducto)
                                Text(
                                    text = "Precio: ${detalle.precioUnitario.formatPrice()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Información de la Cotización
                        detalle.cotizacion?.let { cotizacion ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Datos de la Cotización",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Divider()
                                    
                                    DetalleCampo("Paciente", cotizacion.nombrePaciente)
                                    DetalleCampo("Fecha de Receta", cotizacion.fechaReceta)
                                    
                                    Divider()
                                    
                                    Text(
                                        text = "Graduaciones",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DetalleCampo("Ojo Derecho (OD)", cotizacion.gradoOd?.toString() ?: "N/A")
                                    DetalleCampo("Ojo Izquierdo (OI)", cotizacion.gradoOi?.toString() ?: "N/A")
                                    
                                    Divider()
                                    
                                    Text(
                                        text = "Tipo de Lente",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DetalleCampo("Tipo", cotizacion.tipoLente ?: "N/A")
                                    DetalleCampo("Cristal", cotizacion.tipoCristal ?: "N/A")
                                    
                                    Divider()
                                    
                                    Text(
                                        text = "Tratamientos Adicionales",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DetalleCampo("Antirreflejo", if (cotizacion.antirreflejo == true) "Sí" else "No")
                                    DetalleCampo("Filtro Azul", if (cotizacion.filtroAzul == true) "Sí" else "No")
                                    
                                    Divider()
                                    
                                    Text(
                                        text = "Servicios",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    DetalleCampo("Despacho a Domicilio", if (cotizacion.despachoDomicilio == true) "Sí" else "No")
                                    
                                    cotizacion.valorAprox?.let { valor ->
                                        Divider()
                                        DetalleCampo(
                                            "Valor Aproximado",
                                            "$${String.format("%,.0f", valor)}",
                                            destacado = true
                                        )
                                    }
                                }
                            }
                        } ?: run {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Este producto no tiene cotización asociada",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message ?: "Error al cargar detalle",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onNavigateBack) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleCampo(
    label: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) BluePrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
