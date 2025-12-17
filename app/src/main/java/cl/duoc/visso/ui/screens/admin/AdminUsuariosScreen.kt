package cl.duoc.visso.ui.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.Validation
import cl.duoc.visso.utils.ValidationResult
import cl.duoc.visso.utils.formatRut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    navController: NavController,
    viewModel: AdminUsuariosViewModel = hiltViewModel()
) {
    val usuariosState by viewModel.usuarios.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationState) {
        when (operationState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar((operationState as Resource.Success).data ?: "Operación exitosa")
                viewModel.resetOperationState()
                showCreateDialog = false
                showEditDialog = false
                showDeleteDialog = false
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar((operationState as Resource.Error).message ?: "Error")
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                actions = {
                    IconButton(onClick = { viewModel.cargarUsuarios() }) {
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
            AdminBottomNavigationBar(navController = navController, currentRoute = "admin/usuarios")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BluePrimary
            ) {
                Icon(Icons.Default.Add, "Crear Usuario")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = usuariosState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val usuarios = state.data ?: emptyList()

                if (usuarios.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("No hay usuarios", style = MaterialTheme.typography.titleLarge)
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
                        items(usuarios) { usuario ->
                            UsuarioCard(
                                usuario = usuario,
                                onEdit = {
                                    usuarioSeleccionado = usuario
                                    showEditDialog = true
                                },
                                onDelete = {
                                    usuarioSeleccionado = usuario
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
                        Button(onClick = { viewModel.cargarUsuarios() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        UsuarioFormDialog(
            title = "Crear Usuario",
            onDismiss = { showCreateDialog = false },
            onConfirm = { usuario ->
                viewModel.crearUsuario(usuario)
            },
            isCreate = true
        )
    }

    if (showEditDialog && usuarioSeleccionado != null) {
        UsuarioFormDialog(
            title = "Editar Usuario",
            usuario = usuarioSeleccionado,
            onDismiss = { showEditDialog = false },
            onConfirm = { usuario ->
                viewModel.actualizarUsuario(usuarioSeleccionado!!.id!!, usuario)
            },
            isCreate = false
        )
    }

    if (showDeleteDialog && usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = {
                Text("¿Está seguro de eliminar al usuario \"${usuarioSeleccionado?.nombre} ${usuarioSeleccionado?.apellido}\"?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarUsuario(usuarioSeleccionado!!.id!!) },
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
fun UsuarioCard(
    usuario: Usuario,
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialTheme.shapes.medium,
                color = BluePrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellido}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "RUT: ${usuario.rut}",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    color = if (usuario.rol == "ADMIN")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = usuario.rol ?: "USER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Acciones
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
fun UsuarioFormDialog(
    title: String,
    usuario: Usuario? = null,
    onDismiss: () -> Unit,
    onConfirm: (Usuario) -> Unit,
    isCreate: Boolean
) {
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var apellido by remember { mutableStateOf(usuario?.apellido ?: "") }
    var rut by remember { mutableStateOf(usuario?.rut ?: "") }
    var email by remember { mutableStateOf(usuario?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(usuario?.rol ?: "USER") }
    var passwordVisible by remember { mutableStateOf(false) }

    var rutError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var rolExpanded by remember { mutableStateOf(false) }

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
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rut,
                    onValueChange = { input ->
                        if (input.length <= 12) {
                            rut = input
                            if (isCreate) {
                                rutError = when (val result = Validation.validateRut(rut)) {
                                    is ValidationResult.Error -> result.message
                                    ValidationResult.Success -> null
                                }
                            }
                        }
                    },
                    label = { Text("RUT") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    placeholder = { Text("12.345.678-9") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = isCreate,
                    isError = isCreate && rutError != null,
                    supportingText = if (isCreate) rutError?.let { { Text(it) } } else null
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = when (val result = Validation.validateEmail(it)) {
                            is ValidationResult.Error -> result.message
                            ValidationResult.Success -> null
                        }
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } }
                )

                if (isCreate) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = when {
                                it.isBlank() -> "La contraseña es requerida"
                                it.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                                else -> null
                            }
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } }
                    )
                }

                // Dropdown Rol
                ExposedDropdownMenuBox(
                    expanded = rolExpanded,
                    onExpandedChange = { rolExpanded = !rolExpanded }
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(rolExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = rolExpanded,
                        onDismissRequest = { rolExpanded = false }
                    ) {
                        listOf("USER", "VENDEDOR", "ADMIN").forEach { rolOption ->
                            DropdownMenuItem(
                                text = { Text(rolOption) },
                                onClick = {
                                    rol = rolOption
                                    rolExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isFormValid = nombre.isNotBlank() && 
                apellido.isNotBlank() &&
                rut.isNotBlank() && 
                rutError == null &&
                email.isNotBlank() && 
                emailError == null &&
                (!isCreate || (password.isNotBlank() && passwordError == null))

            Button(
                onClick = {
                    val usuarioData = Usuario(
                        id = usuario?.id,
                        nombre = nombre,
                        apellido = apellido,
                        rut = rut,
                        email = email,
                        passwordHash = if (isCreate) password else null,
                        rol = rol
                    )
                    onConfirm(usuarioData)
                },
                enabled = isFormValid
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}