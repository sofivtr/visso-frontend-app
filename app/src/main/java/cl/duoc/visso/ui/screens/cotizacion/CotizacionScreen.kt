package cl.duoc.visso.ui.screens.cotizacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizacionScreen(
    productoId: Long,
    onNavigateBack: () -> Unit,
    onCotizacionExitosa: () -> Unit,
    viewModel: CotizacionViewModel = hiltViewModel()
) {
    // Cargar el producto por ID
    LaunchedEffect(productoId) {
        viewModel.cargarProducto(productoId)
    }
    
    val productoState by viewModel.productoState.collectAsState()
    
    when (val state = productoState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is Resource.Success -> {
            state.data?.let { producto ->
                CotizacionFormScreen(
                    producto = producto,
                    onNavigateBack = onNavigateBack,
                    onCotizacionExitosa = onCotizacionExitosa,
                    viewModel = viewModel
                )
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
                        text = state.message ?: "Error al cargar producto",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CotizacionFormScreen(
    producto: Producto,
    onNavigateBack: () -> Unit,
    onCotizacionExitosa: () -> Unit,
    viewModel: CotizacionViewModel
) {
    var nombrePaciente by remember { mutableStateOf("") }
    var fechaReceta by remember { mutableStateOf(LocalDate.now().toString()) }
    var gradoOd by remember { mutableStateOf("") }
    var gradoOi by remember { mutableStateOf("") }

    // Dropdown
    var tipoLenteExpanded by remember { mutableStateOf(false) }
    var tipoLenteSeleccionado by remember { mutableStateOf("Monofocal") }
    val opcionesTipoLente = listOf("Monofocal", "Bifocal", "Progresivo")

    // Radio Button
    var tipoCristal by remember { mutableStateOf("Blanco") }

    // Checkboxes
    var antirreflejo by remember { mutableStateOf(false) }
    var filtroAzul by remember { mutableStateOf(false) }
    var despachoDomicilio by remember { mutableStateOf(false) }

    // Validación
    var showError by remember { mutableStateOf(false) }

    val cotizacionState by viewModel.cotizacionState.collectAsState()
    val agregarState by viewModel.agregarState.collectAsState()

    LaunchedEffect(agregarState) {
        when (agregarState) {
            is Resource.Success -> {
                onCotizacionExitosa()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotización de Lente Óptico") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del producto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.getFormattedPrice(),
                        style = MaterialTheme.typography.titleLarge,
                        color = BluePrimary
                    )
                }
            }

            Text(
                text = "Complete los datos del paciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 1. CAMPO DE TEXTO - Nombre Paciente (mín 2 caracteres)
            OutlinedTextField(
                value = nombrePaciente,
                onValueChange = { nombrePaciente = it },
                label = { Text("Nombre del Paciente") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && nombrePaciente.length < 2,
                supportingText = {
                    if (showError && nombrePaciente.length < 2) {
                        Text("Mínimo 2 caracteres")
                    }
                }
            )

            // 2. SELECTOR DE FECHA
            OutlinedTextField(
                value = fechaReceta,
                onValueChange = { fechaReceta = it },
                label = { Text("Fecha de Receta (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.DateRange, null) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2025-01-15") },
                singleLine = true
            )

            // 3. CAMPOS NUMÉRICOS - Graduaciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = gradoOd,
                    onValueChange = { gradoOd = it },
                    label = { Text("Grado OD (Ojo Derecho)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = showError && gradoOd.toDoubleOrNull() == null
                )

                OutlinedTextField(
                    value = gradoOi,
                    onValueChange = { gradoOi = it },
                    label = { Text("Grado OI (Ojo Izquierdo)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = showError && gradoOi.toDoubleOrNull() == null
                )
            }

            // 4. DROPDOWN (Lista Desplegable) - Tipo de Lente
            ExposedDropdownMenuBox(
                expanded = tipoLenteExpanded,
                onExpandedChange = { tipoLenteExpanded = !tipoLenteExpanded }
            ) {
                OutlinedTextField(
                    value = tipoLenteSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Lente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoLenteExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = tipoLenteExpanded,
                    onDismissRequest = { tipoLenteExpanded = false }
                ) {
                    opcionesTipoLente.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                tipoLenteSeleccionado = opcion
                                tipoLenteExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. RADIO BUTTON - Tipo de Cristal
            Text(
                text = "Tipo de Cristal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = tipoCristal == "Blanco",
                            onClick = { tipoCristal = "Blanco" }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoCristal == "Blanco",
                        onClick = { tipoCristal = "Blanco" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Blanco (Transparente)")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = tipoCristal == "Fotocromático",
                            onClick = { tipoCristal = "Fotocromático" }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoCristal == "Fotocromático",
                        onClick = { tipoCristal = "Fotocromático" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fotocromático (Oscurece con sol)")
                }
            }

            // 6. CHECKBOXES - Tratamientos Adicionales
            Text(
                text = "Tratamientos Adicionales",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = antirreflejo,
                    onCheckedChange = { antirreflejo = it }
                )
                Text("Antirreflejo (+$15.000)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = filtroAzul,
                    onCheckedChange = { filtroAzul = it }
                )
                Text("Filtro de Luz Azul (+$10.000)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = despachoDomicilio,
                    onCheckedChange = { despachoDomicilio = it }
                )
                Text("Despacho a Domicilio (+$5.000)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Agregar al Carrito
            Button(
                onClick = {
                    val gradoOdValue = gradoOd.toDoubleOrNull()
                    val gradoOiValue = gradoOi.toDoubleOrNull()

                    if (nombrePaciente.length >= 2 && gradoOdValue != null && gradoOiValue != null) {
                        viewModel.crearCotizacionYAgregarCarrito(
                            producto = producto,
                            nombrePaciente = nombrePaciente,
                            fechaReceta = fechaReceta,
                            gradoOd = gradoOdValue,
                            gradoOi = gradoOiValue,
                            tipoLente = tipoLenteSeleccionado,
                            tipoCristal = tipoCristal,
                            antirreflejo = antirreflejo,
                            filtroAzul = filtroAzul,
                            despachoDomicilio = despachoDomicilio
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = cotizacionState !is Resource.Loading
            ) {
                if (cotizacionState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar al Carrito")
                }
            }

            if (cotizacionState is Resource.Error) {
                Text(
                    text = (cotizacionState as Resource.Error).message ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}