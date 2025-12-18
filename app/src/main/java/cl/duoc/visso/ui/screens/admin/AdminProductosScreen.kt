package cl.duoc.visso.ui.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.visso.data.model.Categoria
import cl.duoc.visso.data.model.Marca
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen(
    navController: NavController,
    viewModel: AdminProductosViewModel = hiltViewModel()
) {
    val productosState by viewModel.productos.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para error de código repetido
    var codigoError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(operationState, showCreateDialog, showEditDialog) {
        when (operationState) {
            is Resource.Success -> {
                showCreateDialog = false
                showEditDialog = false
                showDeleteDialog = false
                codigoError = null
                kotlinx.coroutines.delay(150)
                snackbarHostState.showSnackbar((operationState as Resource.Success).data ?: "Operación exitosa")
                viewModel.resetOperationState()
            }
            is Resource.Error -> {
                val msg = (operationState as Resource.Error).message ?: "Error"
                // Forzar mensaje 'Código existente' si es error genérico al crear
                if (showCreateDialog && msg.trim().equals("Error al crear producto", ignoreCase = true)) {
                    codigoError = "Código existente"
                } else if (msg.trim().equals("Código existente", ignoreCase = true)) {
                    codigoError = "Código existente"
                } else {
                    codigoError = msg
                }
                // No cerrar el modal, solo limpiar el estado de operación
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos") },
                actions = {
                    IconButton(onClick = { viewModel.cargarProductos() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(navController = navController, currentRoute = "admin/productos")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BluePrimary
            ) {
                Icon(Icons.Default.Add, "Crear Producto", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = productosState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val productos = state.data ?: emptyList()

                if (productos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("No hay productos", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(productos) { producto ->
                            ProductoAdminCard(
                                producto = producto,
                                onEdit = {
                                    productoSeleccionado = producto
                                    showEditDialog = true
                                },
                                onDelete = {
                                    productoSeleccionado = producto
                                    showDeleteDialog = true
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
                            text = state.message ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.cargarProductos() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        ProductoFormDialog(
            title = "Crear Producto",
            codigoError = codigoError,
            onCodigoErrorClear = { codigoError = null },
            onDismiss = { showCreateDialog = false },
            onConfirm = { codigo, nombre, desc, precio, stock, imagen, cat, marca ->
                viewModel.crearProducto(codigo, nombre, desc, precio, stock, imagen, cat, marca)
            },
            viewModel = viewModel,
            isEdit = false
        )
    }

    if (showEditDialog && productoSeleccionado != null) {
        ProductoFormDialog(
            title = "Editar Producto",
            producto = productoSeleccionado,
            codigoError = codigoError,
            onCodigoErrorClear = { codigoError = null },
            onDismiss = { showEditDialog = false },
            onConfirm = { codigo, nombre, desc, precio, stock, imagen, cat, marca ->
                viewModel.actualizarProducto(
                    productoSeleccionado!!.id!!,
                    codigo, nombre, desc, precio, stock, imagen, cat, marca,
                    productoSeleccionado!!.fechaCreacion
                )
            },
            viewModel = viewModel,
            isEdit = true
        )
    }

    if (showDeleteDialog && productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Está seguro de eliminar \"${productoSeleccionado?.nombre}\"?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarProducto(productoSeleccionado!!.id!!) },
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
}

@Composable
fun ProductoAdminCard(
    producto: Producto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = producto.getFullImageUrl(),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Código: ${producto.codigoProducto}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = producto.getFormattedPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = BluePrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormDialog(
    title: String,
    producto: Producto? = null,
    codigoError: String? = null,
    onCodigoErrorClear: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double, Int, String, Categoria, Marca) -> Unit,
    viewModel: AdminProductosViewModel,
    isEdit: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categoriasState by viewModel.categorias.collectAsState()
    val marcasState by viewModel.marcas.collectAsState()

    var codigo by remember { mutableStateOf(producto?.codigoProducto ?: "") }
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(producto?.descripcion ?: "") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagenUrl by remember { mutableStateOf(producto?.imagenUrl ?: "") }

    var categoriaSeleccionada by remember { mutableStateOf(producto?.categoria) }
    var marcaSeleccionada by remember { mutableStateOf(producto?.marca) }

    var categoriaExpanded by remember { mutableStateOf(false) }
    var marcaExpanded by remember { mutableStateOf(false) }

    var showImageOptions by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var subiendoImagen by remember { mutableStateOf(false) }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            scope.launch {
                subiendoImagen = true
                val categoria = categoriaSeleccionada?.nombre?.uppercase() ?: "GENERAL"
                Log.d("AdminProductosScreen", "📤 Subiendo imagen con categoría: $categoria")
                val url = viewModel.subirImagenAlServidor(context, it, categoria)
                if (url != null) {
                    imagenUrl = url
                    Log.d("AdminProductosScreen", "✅ Imagen subida exitosamente: $url")
                } else {
                    Log.e("AdminProductosScreen", "❌ Error subiendo imagen")
                }
                subiendoImagen = false
            }
        }
    }

    // Launcher para cámara
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri.value?.let {
                imagenUri = it
                scope.launch {
                    subiendoImagen = true
                    val categoria = categoriaSeleccionada?.nombre?.uppercase() ?: "GENERAL"
                    Log.d("AdminProductosScreen", "📤 Subiendo imagen con categoría: $categoria")
                    val url = viewModel.subirImagenAlServidor(context, it, categoria)
                    if (url != null) {
                        imagenUrl = url
                        Log.d("AdminProductosScreen", "✅ Imagen subida exitosamente: $url")
                    } else {
                        Log.e("AdminProductosScreen", "❌ Error subiendo imagen")
                    }
                    subiendoImagen = false
                }
            }
        }
    }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                Log.d("AdminProductosScreen", "📸 Permiso de cámara concedido, preparando archivo")
                // Permiso concedido, abrir cámara
                val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                if (photoFile.exists()) {
                    photoFile.delete()
                }
                photoFile.createNewFile()
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "cl.duoc.visso.fileprovider",
                    photoFile
                )
                cameraUri.value = uri
                Log.d("AdminProductosScreen", "📸 URI creada: $uri")
                Log.d("AdminProductosScreen", "📸 Lanzando cámara...")
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e("AdminProductosScreen", "❌ Error al abrir cámara", e)
            }
        } else {
            Log.w("AdminProductosScreen", "⚠️ Permiso de cámara denegado")
            showPermissionDialog = true
        }
    }

    // Validación de campos obligatorios para habilitar el botón Guardar
    val camposValidos = codigo.isNotBlank() && nombre.isNotBlank() && descripcion.isNotBlank() &&
        precio.toDoubleOrNull() != null && stock.toIntOrNull() != null &&
        categoriaSeleccionada != null && marcaSeleccionada != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = codigo,
                    onValueChange = {
                        if (!isEdit) {
                            if (it.length <= 10) {
                                codigo = it
                                if (codigoError != null) onCodigoErrorClear?.invoke()
                            }
                        }
                    },
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = codigoError != null,
                    supportingText = codigoError?.let { { Text(it) } },
                    enabled = !isEdit
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Dropdown Categoría
                if (categoriasState is Resource.Success) {
                    val categorias = (categoriasState as Resource.Success).data ?: emptyList()

                    ExposedDropdownMenuBox(
                        expanded = categoriaExpanded,
                        onExpandedChange = { categoriaExpanded = !categoriaExpanded }
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoriaExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = categoriaExpanded,
                            onDismissRequest = { categoriaExpanded = false }
                        ) {
                            categorias.forEach { categoria ->
                                DropdownMenuItem(
                                    text = { Text(categoria.nombre) },
                                    onClick = {
                                        categoriaSeleccionada = categoria
                                        categoriaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Dropdown Marca
                if (marcasState is Resource.Success) {
                    val marcas = (marcasState as Resource.Success).data ?: emptyList()

                    ExposedDropdownMenuBox(
                        expanded = marcaExpanded,
                        onExpandedChange = { marcaExpanded = !marcaExpanded }
                    ) {
                        OutlinedTextField(
                            value = marcaSeleccionada?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Marca") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(marcaExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = marcaExpanded,
                            onDismissRequest = { marcaExpanded = false }
                        ) {
                            marcas.forEach { marca ->
                                DropdownMenuItem(
                                    text = { Text(marca.nombre) },
                                    onClick = {
                                        marcaSeleccionada = marca
                                        marcaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Botón para imagen
                OutlinedButton(
                    onClick = { showImageOptions = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imagenUri != null || imagenUrl.isNotEmpty()) "Cambiar Imagen" else "Agregar Imagen")
                }

                if (imagenUri != null) {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (camposValidos) {
                        onConfirm(
                            codigo, nombre, descripcion,
                            precio.toDouble(), stock.toInt(),
                            imagenUrl,
                            categoriaSeleccionada!!, marcaSeleccionada!!
                        )
                    }
                },
                enabled = !subiendoImagen && (if (!isEdit) camposValidos else true)
            ) {
                if (subiendoImagen) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Dialog de opciones de imagen
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Seleccionar Imagen") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            // Verificar permiso de cámara
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> {
                                    try {
                                        Log.d("AdminProductosScreen", "📸 Permiso ya concedido, lanzando cámara")
                                        showImageOptions = false
                                        // Permiso ya concedido
                                        val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                        if (photoFile.exists()) {
                                            photoFile.delete()
                                        }
                                        photoFile.createNewFile()
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "cl.duoc.visso.fileprovider",
                                            photoFile
                                        )
                                        cameraUri.value = uri
                                        Log.d("AdminProductosScreen", "📸 URI: $uri, lanzando cámara...")
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        Log.e("AdminProductosScreen", "❌ Error al abrir cámara", e)
                                    }
                                }
                                else -> {
                                    Log.d("AdminProductosScreen", "📸 Solicitando permiso de cámara")
                                    showImageOptions = false
                                    // Solicitar permiso
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar Foto")
                    }

                    TextButton(
                        onClick = {
                            showImageOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Desde Galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de permiso denegado
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso Requerido") },
            text = { Text("Se necesita permiso de cámara para tomar fotos. Por favor, habilítalo en la configuración de la aplicación.") },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}