package com.example.serviciocobros.ui.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.UsuarioInsert
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    onBack: () -> Unit,
    onUserAdded: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var empresa by remember { mutableStateOf("") }
    var esAdmin by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(esAdmin) {
        if (esAdmin) {
            empresa = "Administrador"
        } else {
            if (empresa == "Administrador") empresa = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Usuario") },
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
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Información del Usuario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = celular,
                onValueChange = { celular = it },
                label = { Text("Número de Celular (Opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Es Administrador?", fontWeight = FontWeight.Bold)
                        Text("Se asignará la empresa 'Administrador'", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = esAdmin,
                        onCheckedChange = { esAdmin = it }
                    )
                }
            }

            OutlinedTextField(
                value = empresa,
                onValueChange = {
                    if (!esAdmin) empresa = it
                },
                label = { Text("Empresa / Negocio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !esAdmin,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                supportingText = {
                    if (esAdmin) Text("Bloqueado: Valor fijo para administradores")
                    else Text("El nombre de la empresa del cliente")
                }
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
                        if (nombre.isBlank() || correo.isBlank() || password.isBlank()) {
                            errorMessage = "Completa nombre, correo y contraseña."
                            return@launch
                        }

                        if (!esAdmin) {
                            if (empresa.isBlank()) {
                                errorMessage = "Debes ingresar el nombre de la empresa."
                                return@launch
                            }
                            if (empresa.contains("administrador", ignoreCase = true)) {
                                errorMessage = "El nombre de empresa no puede contener la palabra 'Administrador'."
                                return@launch
                            }
                        }

                        isSaving = true
                        errorMessage = ""

                        val nuevoUsuario = UsuarioInsert(
                            nombre = nombre,
                            correo = correo,
                            contrasena = password,
                            esAdmin = esAdmin,
                            celular = celular.ifBlank { null },
                            empresa = empresa
                        )

                        val exito = SupabaseClient.crearUsuario(nuevoUsuario)

                        isSaving = false
                        if (exito) {
                            Toast.makeText(context, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                            onUserAdded()
                        } else {
                            errorMessage = "Error al crear. Verifica si el correo ya existe."
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Text("REGISTRAR USUARIO")
                }
            }
        }
    }
}