package com.example.serviciocobros.ui.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Plato
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuScreen(
    onBack: () -> Unit,
    onEditClick: (Plato) -> Unit
) {
    var platos by remember { mutableStateOf<List<Plato>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }
    var platoToDelete by remember { mutableStateOf<Plato?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarPlatos() {
        scope.launch {
            isLoading = true
            platos = SupabaseClient.obtenerPlatos()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarPlatos()
    }

    if (showDialog && platoToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("Se eliminará \"${platoToDelete?.nombre}\" del menú. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val exito = SupabaseClient.eliminarPlato(platoToDelete!!.id)
                            if (exito) {
                                Toast.makeText(context, "Plato eliminado", Toast.LENGTH_SHORT).show()
                                cargarPlatos()
                            } else {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                            platoToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Menú") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (platos.isEmpty()) {
                Text(
                    text = "No hay platos registrados.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(platos) { plato ->
                        EditPlatoItem(
                            plato = plato,
                            onDeleteClick = {
                                platoToDelete = plato
                                showDialog = true
                            },
                            onItemClick = {
                                onEditClick(plato)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditPlatoItem(
    plato: Plato,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageModifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))

            if (plato.fotoUrl.isNullOrBlank()) {
                Box(
                    modifier = imageModifier.background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White)
                }
            } else {
                AsyncImage(
                    model = plato.fotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plato.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bs. ${plato.precio}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF388E3C)
                )
                Text(
                    text = "Toca para editar",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}