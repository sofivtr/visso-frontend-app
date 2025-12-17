// ========================================
// ui/screens/carrito/CarritoScreen.kt - MEJORADO CON ELIMINAR Y VER DETALLES
// ========================================
package cl.duoc.visso.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.visso.data.model.Cotizacion
import cl.duoc.visso.data.model.DetalleCarrito
import cl.duoc.visso.ui.components.BottomNavigationBar
import cl.duoc.visso.ui.navigation.Screen
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavController,
    viewModel: CarritoViewModel = hiltViewModel()
) {
    val carritoState by viewModel.carrito.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var detalleAEliminar by remember { mutableStateOf<DetalleCarrito?>(null) }
    var cotizacionAMostrar by remember { mutableStateOf<Cotizacion?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(checkoutState) {
        when (checkoutState) {
            is Resource.Success -> {
                showCheckoutDialog = true
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Producto eliminado")
                viewModel.resetDeleteState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar("Error al eliminar")
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "carrito")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = carritoState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val carrito = state.data
                if (carrito?.detalles?.isEmpty() == true) {
                    EmptyCart()
                } else {
                    CarritoContent(
                        carrito = carrito,
                        onCheckout = { viewModel.finalizarCompra() },
                        onDelete = { detalle ->
                            detalleAEliminar = detalle
                            showDeleteDialog = true
                        },
                        onVerDetalle = { cotizacion ->
                            cotizacionAMostrar = cotizacion
                        },
                        checkoutLoading = checkoutState is Resource.Loading,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Error al cargar carrito",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Dialog de confirmación de eliminación
    if (showDeleteDialog && detalleAEliminar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = {
                Text("¿Estás seguro de eliminar \"${detalleAEliminar?.producto?.nombre}\" del carrito?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarDelCarrito(detalleAEliminar?.id ?: 0)
                        showDeleteDialog = false
                        detalleAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de detalles de cotización
    if (cotizacionAMostrar != null) {
        AlertDialog(
            onDismissRequest = { cotizacionAMostrar = null },
            title = { Text("Detalles de Cotización") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetalleCotizacion("Paciente", cotizacionAMostrar!!.nombrePaciente)
                    DetalleCotizacion("Fecha Receta", cotizacionAMostrar!!.fechaReceta)
                    DetalleCotizacion("Grado OD", cotizacionAMostrar!!.gradoOd.toString())
                    DetalleCotizacion("Grado OI", cotizacionAMostrar!!.gradoOi.toString())
                    DetalleCotizacion("Tipo Lente", cotizacionAMostrar!!.tipoLente)
                    DetalleCotizacion("Tipo Cristal", cotizacionAMostrar!!.tipoCristal)

                    if (cotizacionAMostrar!!.antirreflejo) {
                        Text("✓ Antirreflejo", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (cotizacionAMostrar!!.filtroAzul) {
                        Text("✓ Filtro Azul", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (cotizacionAMostrar!!.despachoDomicilio) {
                        Text("✓ Despacho a Domicilio", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { cotizacionAMostrar = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Dialog de compra exitosa
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("¡Compra Exitosa!") },
            text = { Text("Tu pedido ha sido procesado correctamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        viewModel.resetCheckoutState()
                        viewModel.loadCarrito()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun DetalleCotizacion(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyCart() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tu carrito está vacío",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun CarritoContent(
    carrito: cl.duoc.visso.data.model.Carrito?,
    onCheckout: () -> Unit,
    onDelete: (DetalleCarrito) -> Unit,
    onVerDetalle: (Cotizacion) -> Unit,
    checkoutLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            carrito?.detalles?.let { detalles ->
                items(detalles) { detalle ->
                    CarritoItemCard(
                        detalle = detalle,
                        onDelete = { onDelete(detalle) },
                        onVerDetalle = { detalle.cotizacion?.let { onVerDetalle(it) } }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = carrito?.getFormattedTotal() ?: "$0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !checkoutLoading
                ) {
                    if (checkoutLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Finalizar Compra")
                    }
                }
            }
        }
    }
}

@Composable
fun CarritoItemCard(
    detalle: DetalleCarrito,
    onDelete: () -> Unit,
    onVerDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = detalle.producto?.getFullImageUrl(),
                contentDescription = detalle.nombreProducto,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = detalle.nombreProducto,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cantidad: ${detalle.cantidad}",
                    style = MaterialTheme.typography.bodySmall
                )

                // Si tiene cotización, mostrar indicador
                if (detalle.tieneCotizacion()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Cotización personalizada",
                            style = MaterialTheme.typography.bodySmall,
                            color = BluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detalle.getFormattedSubtotal(),
                    style = MaterialTheme.typography.titleMedium,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botones de acción
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón ver detalles (solo si tiene cotización)
                if (detalle.tieneCotizacion()) {
                    IconButton(onClick = onVerDetalle) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver Detalles",
                            tint = BluePrimary
                        )
                    }
                }

                // Botón eliminar
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}