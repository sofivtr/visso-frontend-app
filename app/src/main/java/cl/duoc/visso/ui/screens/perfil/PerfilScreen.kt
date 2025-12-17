package cl.duoc.visso.ui.screens.perfil

import androidx.compose.foundation.layout.*
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
import cl.duoc.visso.data.model.Usuario
import cl.duoc.visso.ui.components.BottomNavigationBar
import cl.duoc.visso.ui.navigation.Screen
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import cl.duoc.visso.utils.Validation
import cl.duoc.visso.utils.ValidationResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val usuarioState by viewModel.usuario.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    var emailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuarioState) {
        if (usuarioState is Resource.Success) {
            val usuario = (usuarioState as Resource.Success).data
            nombre = usuario?.nombre ?: ""
            apellido = usuario?.apellido ?: ""
            email = usuario?.email ?: ""
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            isEditing = false
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                actions = {
                    if (!isEditing && usuarioState is Resource.Success) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "perfil")
        }
    ) { padding ->
        when (val state = usuarioState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icono de perfil
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                        tint = BluePrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        // Modo edición
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it },
                            label = { Text("Apellido") },
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it) } }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }

                            val isFormValid = nombre.isNotBlank() && 
                                apellido.isNotBlank() &&
                                email.isNotBlank() && 
                                emailError == null

                            Button(
                                onClick = {
                                    val usuarioActualizado = state.data?.copy(
                                        nombre = nombre,
                                        apellido = apellido,
                                        email = email
                                    )
                                    usuarioActualizado?.let {
                                        viewModel.actualizarPerfil(it)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = updateState !is Resource.Loading && isFormValid
                            ) {
                                if (updateState is Resource.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Guardar")
                                }
                            }
                        }
                    } else {
                        // Modo visualización
                        ProfileInfoItem(
                            label = "Nombre",
                            value = state.data?.nombre ?: ""
                        )

                        ProfileInfoItem(
                            label = "Apellido",
                            value = state.data?.apellido ?: ""
                        )

                        ProfileInfoItem(
                            label = "RUT",
                            value = state.data?.rut ?: ""
                        )

                        ProfileInfoItem(
                            label = "Email",
                            value = state.data?.email ?: ""
                        )

                        ProfileInfoItem(
                            label = "Rol",
                            value = state.data?.rol ?: "USER"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón cerrar sesión
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.ExitToApp, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Error al cargar perfil",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Dialog de confirmación de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.cerrarSesion()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Sí, cerrar sesión")
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

@Composable
fun ProfileInfoItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider()
    }
}