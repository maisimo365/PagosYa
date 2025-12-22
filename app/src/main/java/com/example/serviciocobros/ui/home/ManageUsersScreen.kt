package com.example.serviciocobros.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onBack: () -> Unit
) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var userToToggle by remember { mutableStateOf<Usuario?>(null) }
    var deudaTotal by remember { mutableStateOf(0.0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun cargarUsuarios() {
        scope.launch {
            isLoading = true
            usuarios = SupabaseClient.obtenerTodosLosClientes()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarUsuarios()
    }

    fun toggleUsuario(usuario: Usuario) {
        scope.launch {
            if (usuario.activo) {
                isLoading = true
                deudaTotal = SupabaseClient.obtenerDeudaTotalUsuario(usuario.id)
                isLoading = false
                userToToggle = usuario
                showDialog = true
            } else {
                val exito = SupabaseClient.activarUsuario(usuario.id)
                if (exito) {
                    Toast.makeText(context, "Usuario activado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(context, "Error al activar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDialog && userToToggle != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                if (deudaTotal > 0) Icon(Icons.Default.Warning, null, tint = Color(0xFFF2994A))
                else Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text(if (deudaTotal > 0) "¡Deuda Pendiente!" else "¿Desactivar Usuario?")
            },
            text = {
                Column {
                    Text("Vas a desactivar a ${userToToggle?.nombre}.")
                    if (deudaTotal > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tiene una deuda de: Bs. $deudaTotal",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val exito = SupabaseClient.desactivarUsuario(userToToggle!!.id)
                            if (exito) {
                                Toast.makeText(context, "Usuario desactivado", Toast.LENGTH_SHORT).show()
                                cargarUsuarios()
                            }
                            showDialog = false
                            userToToggle = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Usuarios") },
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
            } else if (usuarios.isEmpty()) {
                Text("No hay usuarios.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuarios) { usuario ->
                        UserManagementItem(
                            usuario = usuario,
                            onToggleClick = { toggleUsuario(usuario) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementItem(
    usuario: Usuario,
    onToggleClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.activo) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (usuario.activo) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (usuario.activo) Color.Unspecified else Color.Gray
                )
                Text(
                    text = if (usuario.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (usuario.activo) Color(0xFF27AE60) else Color.Red
                )
            }

            if (usuario.activo) {
                IconButton(onClick = onToggleClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Desactivar", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(onClick = onToggleClick) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Activar", tint = Color(0xFF27AE60))
                }
            }
        }
    }
}