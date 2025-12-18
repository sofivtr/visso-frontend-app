package cl.duoc.visso.ui.screens.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.visso.R
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.ui.components.BottomNavigationBar
import cl.duoc.visso.ui.navigation.Screen
import cl.duoc.visso.ui.theme.BluePrimary
import cl.duoc.visso.utils.Resource
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val productos by viewModel.filteredProducts.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val addToCartState by viewModel.addToCartState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para mostrar dialog de cotización
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var showCotizacionDialog by remember { mutableStateOf(false) }

    // Estados para la animación del carrito
    var showFlyingCart by remember { mutableStateOf(false) }
    var startPosition by remember { mutableStateOf(Offset.Zero) }
    var endPosition by remember { mutableStateOf(Offset.Zero) }
    var cartIconPosition by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(addToCartState) {
        when (addToCartState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Producto agregado al carrito")
                viewModel.resetAddToCartState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(
                    (addToCartState as Resource.Error).message ?: "Error"
                )
                viewModel.resetAddToCartState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo Visso",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visso")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BluePrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            // Capturar la posición del ícono del carrito (segundo item de 4)
                            val width = coordinates.size.width
                            // Hay 4 ítems: Home, Carrito, Perfil, Clima
                            // El carrito es el segundo ítem (índice 1)
                            val itemWidth = width / 4f
                            cartIconPosition = Offset(
                                x = itemWidth + (itemWidth / 2f), // Centro del segundo ítem = inicio (1/4) + mitad del ítem
                                y = coordinates.positionInRoot().y + coordinates.size.height / 2f
                            )
                        }
                ) {
                    BottomNavigationBar(navController = navController, currentRoute = "home")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
            if (categorias is Resource.Success) {
                CategoriesFilter(
                    categorias = (categorias as Resource.Success).data ?: emptyList(),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos disponibles")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(productos) { producto ->
                        ProductCard(
                            producto = producto,
                            onAddToCart = { buttonPosition ->
                                // LÓGICA CRÍTICA: Interceptar si es lente óptico
                                Log.d("HomeScreen", "=== DEBUG COTIZACIÓN ===")
                                Log.d("HomeScreen", "Producto: ${producto.nombre}")
                                Log.d("HomeScreen", "Categoría nombre: '${producto.categoria.nombre}'")
                                Log.d("HomeScreen", "esLenteOptico(): ${producto.esLenteOptico()}")
                                
                                if (producto.esLenteOptico()) {
                                    Log.d("HomeScreen", "✓ Es lente óptico - Mostrando dialog")
                                    productoSeleccionado = producto
                                    showCotizacionDialog = true
                                } else {
                                    Log.d("HomeScreen", "✗ NO es lente óptico - Agregando directo al carrito")
                                    // Iniciar animación
                                    startPosition = buttonPosition
                                    endPosition = cartIconPosition
                                    showFlyingCart = true
                                    
                                    viewModel.agregarAlCarrito(producto.id ?: 0)
                                }
                            }
                        )
                    }
                }
            }
            }
        }
        
        // Animación del carrito volador
        if (showFlyingCart) {
            FlyingCartAnimation(
                startPosition = startPosition,
                endPosition = endPosition,
                onAnimationEnd = { showFlyingCart = false }
            )
        }
    }

    // Dialog para redirigir a cotización
    if (showCotizacionDialog && productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showCotizacionDialog = false },
            title = { Text("Lente Óptico") },
            text = {
                Text("Este producto requiere una cotización personalizada. ¿Desea proceder con el formulario de cotización?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCotizacionDialog = false
                        // Navegar a pantalla de cotización pasando el ID del producto
                        navController.navigate("cotizacion/${productoSeleccionado?.id}")
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCotizacionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FlyingCartAnimation(
    startPosition: Offset,
    endPosition: Offset,
    onAnimationEnd: () -> Unit
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }

    val progress = animationProgress.value
    val currentX = startPosition.x + (endPosition.x - startPosition.x) * progress
    val currentY = startPosition.y + (endPosition.y - startPosition.y) * progress
    
    // Efecto de curva parabólica para simular vuelo
    val curveOffset = -150f * kotlin.math.sin(Math.PI.toFloat() * progress)
    
    // Escala que disminuye al acercarse al destino
    val scale = 1f + (0.5f - 1f) * progress
    
    // Alpha que se desvanece al final
    val alpha = if (progress > 0.8f) {
        1f + (0f - 1f) * ((progress - 0.8f) / 0.2f)
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier
                .size(32.dp)
                .offset {
                    IntOffset(
                        x = currentX.roundToInt(),
                        y = (currentY + curveOffset).roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }
}

@Composable
fun ProductCard(
    producto: Producto,
    onAddToCart: (Offset) -> Unit
) {
    var buttonPosition by remember { mutableStateOf(Offset.Zero) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(producto.getFullImageUrl())
                    .crossfade(true)
                    .memoryCacheKey(producto.imagenUrl) // Usar imagenUrl como key para caché
                    .build(),
                contentDescription = producto.nombre,
                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                error = painterResource(android.R.drawable.ic_menu_report_image),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = producto.marca.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = producto.getFormattedPrice(),
                        style = MaterialTheme.typography.titleLarge,
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onAddToCart(buttonPosition) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            buttonPosition = Offset(
                                x = coordinates.positionInRoot().x + coordinates.size.width / 2f,
                                y = coordinates.positionInRoot().y + coordinates.size.height / 2f
                            )
                        },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }
        }
    }
}

@Composable
fun CategoriesFilter(
    categorias: List<cl.duoc.visso.data.model.Categoria>,
    selectedCategory: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Todos") }
            )
        }
        items(categorias) { categoria ->
            FilterChip(
                selected = selectedCategory == categoria.id,
                onClick = { onCategorySelected(categoria.id) },
                label = { Text(categoria.nombre) }
            )
        }
    }
}