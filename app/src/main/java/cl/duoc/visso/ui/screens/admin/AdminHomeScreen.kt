package cl.duoc.visso.ui.screens.admin

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
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel(),
    sessionManager: SessionManager = androidx.compose.ui.platform.LocalContext.current.let { SessionManager(it) }
) {
    val ventasState by viewModel.ventas.collectAsState()
    var showActiveOnly by remember { mutableStateOf(false) }
    var expandedPedidoId by remember { mutableStateOf<Long?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos") },
                actions = {
                    IconButton(onClick = { viewModel.cargarVentas() }) {
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
            AdminBottomNavigationBar(navController = navController, currentRoute = "admin/home")
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

            when (val state = ventasState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val ventas = state.data?.filter {
                        if (showActiveOnly) it.estado == "A" else true
                    } ?: emptyList()

                    if (ventas.isEmpty()) {
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
                            items(ventas) { venta ->
                                PedidoCard(
                                    venta = venta,
                                    isExpanded = expandedPedidoId == venta.id,
                                    onExpandClick = {
                                        expandedPedidoId = if (expandedPedidoId == venta.id) null else venta.id
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
                            Button(onClick = { viewModel.cargarVentas() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación de cierre de sesión
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
fun PedidoCard(
    venta: Carrito,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
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
                    text = "Pedido #${venta.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = if (venta.estado == "A")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (venta.estado == "A") "ACTIVO" else "CERRADO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (venta.estado == "A")
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
                        text = "${venta.usuario.nombre} ${venta.usuario.apellido}",
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
                        text = formatearFecha(venta.fechaCreacion),
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
                    text = "${venta.detalles.size} producto(s)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón expandir/colapsar productos
            if (venta.detalles.isNotEmpty()) {
                TextButton(
                    onClick = onExpandClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isExpanded) "Ocultar productos" else "Ver productos")
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null
                    )
                }
            }

            // Lista de productos (expandible)
            if (isExpanded && venta.detalles.isNotEmpty()) {
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
                        venta.detalles.forEach { detalle ->
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
                            if (detalle != venta.detalles.last()) {
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
                    text = venta.getFormattedTotal(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }
        }
    }
}

fun formatearFecha(fechaString: String): String {
    return try {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = formatoEntrada.parse(fechaString)
        fecha?.let { formatoSalida.format(it) } ?: fechaString
    } catch (e: Exception) {
        fechaString
    }
}