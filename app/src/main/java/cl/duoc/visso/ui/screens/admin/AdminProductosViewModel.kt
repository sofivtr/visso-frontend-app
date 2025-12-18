package cl.duoc.visso.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.visso.data.model.Categoria
import cl.duoc.visso.data.model.Marca
import cl.duoc.visso.data.model.Producto
import cl.duoc.visso.data.repository.CategoriaRepository
import cl.duoc.visso.data.repository.MarcaRepository
import cl.duoc.visso.data.repository.ProductoRepository
import cl.duoc.visso.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class AdminProductosViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val marcaRepository: MarcaRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<Resource<List<Producto>>>(Resource.Loading())
    val productos: StateFlow<Resource<List<Producto>>> = _productos

    private val _categorias = MutableStateFlow<Resource<List<Categoria>>>(Resource.Loading())
    val categorias: StateFlow<Resource<List<Categoria>>> = _categorias

    private val _marcas = MutableStateFlow<Resource<List<Marca>>>(Resource.Loading())
    val marcas: StateFlow<Resource<List<Marca>>> = _marcas

    private val _operationState = MutableStateFlow<Resource<String>?>(null)
    val operationState: StateFlow<Resource<String>?> = _operationState

    init {
        cargarProductos()
        cargarCategorias()
        cargarMarcas()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _productos.value = Resource.Loading()
            _productos.value = productoRepository.listarProductos()
        }
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            _categorias.value = Resource.Loading()
            _categorias.value = categoriaRepository.obtenerCategorias()
        }
    }

    private fun cargarMarcas() {
        viewModelScope.launch {
            _marcas.value = Resource.Loading()
            _marcas.value = marcaRepository.obtenerMarcas()
        }
    }

    fun crearProducto(
        codigo: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        stock: Int,
        imagenUrl: String,
        categoria: Categoria,
        marca: Marca
    ) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            Log.d("AdminProductosViewModel", "Creando producto con imagenUrl: $imagenUrl")
            // Usar formato ISO para la fecha (YYYY-MM-DD)
            val fechaActual = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            val producto = Producto(
                codigoProducto = codigo,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                fechaCreacion = fechaActual,
                imagenUrl = imagenUrl,
                categoria = categoria,
                marca = marca
            )
            val resultado = productoRepository.crearProducto(producto)
            _operationState.value = when (resultado) {
                is Resource.Success -> {
                    Log.d("AdminProductosViewModel", "Producto creado, recargando lista")
                    cargarProductos()
                    Resource.Success("Producto creado exitosamente")
                }
                is Resource.Error -> Resource.Error(resultado.message ?: "Error al crear producto")
                else -> Resource.Error("Error desconocido")
            }
        }
    }

    fun actualizarProducto(
        id: Long,
        codigo: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        stock: Int,
        imagenUrl: String,
        categoria: Categoria,
        marca: Marca,
        fechaCreacion: String
    ) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            Log.d("AdminProductosViewModel", "Actualizando producto con imagenUrl: $imagenUrl")
            val producto = Producto(
                id = id,
                codigoProducto = codigo,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                fechaCreacion = fechaCreacion,
                imagenUrl = imagenUrl,
                categoria = categoria,
                marca = marca
            )
            val resultado = productoRepository.actualizarProducto(id, producto)
            _operationState.value = when (resultado) {
                is Resource.Success -> {
                    Log.d("AdminProductosViewModel", "Producto actualizado, recargando lista")
                    cargarProductos()
                    Resource.Success("Producto actualizado exitosamente")
                }
                is Resource.Error -> Resource.Error(resultado.message ?: "Error al actualizar producto")
                else -> Resource.Error("Error desconocido")
            }
        }
    }

    fun eliminarProducto(id: Long) {
        viewModelScope.launch {
            _operationState.value = Resource.Loading()
            val resultado = productoRepository.eliminarProducto(id)
            _operationState.value = when (resultado) {
                is Resource.Success -> {
                    cargarProductos()
                    Resource.Success("Producto eliminado exitosamente")
                }
                is Resource.Error -> Resource.Error(resultado.message ?: "Error al eliminar producto")
                else -> Resource.Error("Error desconocido")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    suspend fun subirImagenAlServidor(context: Context, uri: Uri, categoria: String = "GENERAL"): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val timestamp = System.currentTimeMillis()
            val fileName = "producto_$timestamp.jpg" // Timestamp para nombres únicos
            
            Log.d("AdminProductosViewModel", "Preparando subida: $fileName, categoría: $categoria")
            
            val bytes = inputStream?.readBytes() ?: return null
            
            // Comprimir imagen si es muy grande (más de 1MB)
            val compressedBytes = if (bytes.size > 1_000_000) {
                Log.d("AdminProductosViewModel", "Comprimiendo imagen: ${bytes.size} bytes")
                compressImage(context, uri) ?: bytes
            } else {
                bytes
            }
            
            Log.d("AdminProductosViewModel", "Tamaño final: ${compressedBytes.size} bytes")
            
            val requestFile = compressedBytes.toRequestBody(
                "image/*".toMediaType()
            )
            
            val imagenPart = MultipartBody.Part.createFormData("imagen", fileName, requestFile)
            
            when (val result = productoRepository.subirImagen(imagenPart, categoria)) {
                is Resource.Success -> {
                    Log.d("AdminProductosViewModel", "Imagen subida: ${result.data}")
                    result.data
                }
                is Resource.Error -> {
                    Log.e("AdminProductosViewModel", "Error subiendo imagen: ${result.message}")
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("AdminProductosViewModel", "Error procesando imagen", e)
            null
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            bitmap.recycle()
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("AdminProductosViewModel", "Error comprimiendo imagen", e)
            null
        }
    }
}
