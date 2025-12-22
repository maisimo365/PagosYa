package com.example.serviciocobros.ui.menu

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.ui.home.processImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishScreen(
    plato: Plato,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(plato.nombre) }
    var precioStr by remember { mutableStateOf(plato.precio.toString()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Plato") },
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
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
                        contentDescription = "Nueva Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!plato.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = plato.fotoUrl,
                        contentDescription = "Foto Actual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Text("Sin imagen. Toca para agregar.", color = Color.Gray)
                    }
                }
            }

            Text("Toca la imagen para cambiarla", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del plato") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioStr,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) precioStr = it },
                label = { Text("Precio (Bs.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        errorMessage = ""
                        val precio = precioStr.toDoubleOrNull()

                        if (nombre.isBlank() || precio == null) {
                            errorMessage = "Datos inválidos"
                            isSaving = false
                            return@launch
                        }


                        val exito = withContext(Dispatchers.IO) {
                            var nuevaFotoUrl: String? = null

                            if (selectedImageUri != null) {
                                val bytes = processImage(context, selectedImageUri!!)
                                if (bytes != null) {
                                    nuevaFotoUrl = SupabaseClient.subirImagenPlato(bytes)
                                }
                            }

                            SupabaseClient.actualizarPlato(plato.id, nombre, precio, nuevaFotoUrl)
                        }

                        if (exito) {
                            Toast.makeText(context, "Plato actualizado", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            errorMessage = "Error al actualizar."
                        }
                        isSaving = false
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("GUARDAR CAMBIOS")
            }
        }
    }
}