package com.example.serviciocobros.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.PlatoInsert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDishScreen(
    onBack: () -> Unit,
    onDishAdded: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    fun createTempPictureUri(): Uri? {
        return try {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val tempFile = File.createTempFile(
                "foto_${System.currentTimeMillis()}",
                ".jpg",
                cacheDir
            )
            FileProvider.getUriForFile(
                context,
                "com.example.serviciocobros.provider",
                tempFile
            )
        } catch (e: Exception) {
            Log.e("AddDish", "Error creando archivo temporal: ${e.message}")
            Toast.makeText(context, "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
            null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Plato") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Detalles del Producto", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Toca para agregar foto", color = Color.Gray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = createTempPictureUri()
                        if (uri != null) {
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galería")
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del plato") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioStr,
                onValueChange = { input ->
                    if (input.all { char -> char.isDigit() || char == '.' }) {
                        precioStr = input
                    }
                },
                label = { Text("Precio (Bs.)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        errorMessage = ""

                        val precio = precioStr.toDoubleOrNull()
                        if (nombre.isBlank() || precio == null) {
                            errorMessage = "Completa el nombre y un precio válido."
                            isSaving = false
                            return@launch
                        }

                        try {
                            val exito = withContext(Dispatchers.IO) {
                                var finalFotoUrl: String? = null

                                if (selectedImageUri != null) {
                                    val imageBytes = processImage(context, selectedImageUri!!)
                                    if (imageBytes != null) {
                                        finalFotoUrl = SupabaseClient.subirImagenPlato(imageBytes)
                                        if (finalFotoUrl == null) {
                                            return@withContext false
                                        }
                                    } else {
                                        return@withContext false
                                    }
                                }

                                val nuevoPlato = PlatoInsert(
                                    nombre = nombre,
                                    precio = precio,
                                    fotoUrl = finalFotoUrl
                                )
                                SupabaseClient.crearPlato(nuevoPlato)
                            }

                            if (exito) {
                                onDishAdded()
                            } else {
                                errorMessage = "Error al subir la imagen o guardar. Verifica tu conexión."
                            }

                        } catch (e: Exception) {
                            Log.e("AddDishScreen", "Excepción al guardar: ${e.message}")
                            e.printStackTrace()
                            errorMessage = "Error inesperado: ${e.localizedMessage}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("GUARDAR PLATO")
                }
            }
        }
    }
}


fun processImage(context: Context, uri: Uri): ByteArray? {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var scale = 1
        val targetSize = 1024
        while (options.outWidth / scale / 2 >= targetSize && options.outHeight / scale / 2 >= targetSize) {
            scale *= 2
        }

        val loadOptions = BitmapFactory.Options().apply { inSampleSize = scale }
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, loadOptions)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

        bitmap?.recycle()

        outputStream.toByteArray()
    } catch (e: Exception) {
        Log.e("ImageProcess", "Error procesando imagen: ${e.message}")
        null
    }
}