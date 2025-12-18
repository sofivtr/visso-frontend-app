package cl.duoc.visso.ui.screens.vendedor

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.visso.data.model.Carrito
import cl.duoc.visso.data.model.Cotizacion
import cl.duoc.visso.ui.screens.admin.PedidoCard
import cl.duoc.visso.ui.screens.admin.formatearFecha
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendedorHomeScreen(
    navController: NavController,
    viewModel: VendedorViewModel = hiltViewModel(),
    sessionManager: SessionManager = androidx.compose.ui.platform.LocalContext.current.let { SessionManager(it) }
) {
    val pedidosState by viewModel.pedidos.collectAsState()
    var showActiveOnly by remember { mutableStateOf(false) }
    var expandedPedidoId by remember { mutableStateOf<Long?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var cotizacionSeleccionada by remember { mutableStateOf<Cotizacion?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos - Vendedor") },
                actions = {
                    IconButton(onClick = { viewModel.cargarPedidos() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            VendedorBottomNavigationBar(navController = navController, currentRoute = "vendedor/home")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros de estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !showActiveOnly,
                    onClick = { showActiveOnly = false },
                    label = { Text("Todos") },
                    leadingIcon = if (!showActiveOnly) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = showActiveOnly,
                    onClick = { showActiveOnly = true },
                    label = { Text("Solo Activos") },
                    leadingIcon = if (showActiveOnly) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }

            when (val state = pedidosState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val pedidos = state.data?.filter {
                        if (showActiveOnly) it.estado == "A" else true
                    } ?: emptyList()

                    if (pedidos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (showActiveOnly) "No hay pedidos activos" else "No hay pedidos",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pedidos) { pedido ->
                                VendedorPedidoCard(
                                    pedido = pedido,
                                    isExpanded = expandedPedidoId == pedido.id,
                                    onExpandClick = {
                                        expandedPedidoId = if (expandedPedidoId == pedido.id) null else pedido.id
                                    },
                                    onVerCotizacion = { cotizacion ->
                                        cotizacionSeleccionada = cotizacion
                                    }
                                )
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
                                text = state.message ?: "Error al cargar pedidos",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.cargarPedidos() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }

        // Dialog de cotización
        if (cotizacionSeleccionada != null) {
            CotizacionModal(
                cotizacion = cotizacionSeleccionada!!,
                onDismiss = { cotizacionSeleccionada = null }
            )
        }

        // Dialog de cierre de sesión
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                sessionManager.clearSession()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text("Cerrar Sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun VendedorPedidoCard(
    pedido: Carrito,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onVerCotizacion: (Cotizacion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = if (pedido.estado == "A")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pedido.estado == "A") "ACTIVO" else "CERRADO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pedido.estado == "A")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Divider()

            // Cliente
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Cliente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pedido.usuario.nombre} ${pedido.usuario.apellido}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Fecha
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatearFecha(pedido.fechaCreacion),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Productos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${pedido.detalles.size} producto(s)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón expandir/colapsar
            if (pedido.detalles.isNotEmpty()) {
                TextButton(
                    onClick = onExpandClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isExpanded) "Ocultar detalles" else "Ver detalles")
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null
                    )
                }
            }

            // Detalles expandidos
            if (isExpanded && pedido.detalles.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Detalle de productos:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        pedido.detalles.forEach { detalle ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = detalle.nombreProducto,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Cantidad: ${detalle.cantidad}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = detalle.getFormattedSubtotal(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                }

                                // Botón Ver Cotización si existe
                                if (detalle.cotizacion != null) {
                                    Button(
                                        onClick = { onVerCotizacion(detalle.cotizacion!!) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BluePrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Visibility, "Ver Cotización")
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ver Cotización")
                                    }
                                }
                            }
                            if (detalle != pedido.detalles.last()) {
                                Divider()
                            }
                        }
                    }
                }
            }

            Divider()

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pedido.getFormattedTotal(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }
        }
    }
}

@Composable
fun CotizacionModal(
    cotizacion: Cotizacion,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Detalle de Cotización",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CotizacionDetalle("Paciente", cotizacion.nombrePaciente)
                CotizacionDetalle("Fecha Receta", cotizacion.fechaReceta)

                Divider()

                Text(
                    "Graduaciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                CotizacionDetalle("Ojo Derecho (OD)", cotizacion.gradoOd.toString())
                CotizacionDetalle("Ojo Izquierdo (OI)", cotizacion.gradoOi.toString())

                Divider()

                Text(
                    "Tipo de Lente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                CotizacionDetalle("Tipo", cotizacion.tipoLente)
                CotizacionDetalle("Cristal", cotizacion.tipoCristal)

                Divider()

                Text(
                    "Tratamientos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                CotizacionDetalle("Antirreflejo", if (cotizacion.antirreflejo) "Sí" else "No")
                CotizacionDetalle("Filtro Azul", if (cotizacion.filtroAzul) "Sí" else "No")

                Divider()

                CotizacionDetalle("Despacho a Domicilio", if (cotizacion.despachoDomicilio) "Sí" else "No")

                cotizacion.valorAprox?.let {
                    Divider()
                    CotizacionDetalle(
                        "Valor Aproximado",
                        "$${"%.0f".format(it)}",
                        destacado = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun CotizacionDetalle(
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
            color = if (destacado) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) BluePrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}