package com.example.serviciocobros

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Usuario
import com.example.serviciocobros.ui.home.AdminDashboardScreen // <--- Importamos pantalla Admin
import com.example.serviciocobros.ui.home.UserHomeScreen      // <--- Importamos pantalla Usuario
import com.example.serviciocobros.ui.theme.ServicioCobrosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServicioCobrosTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    if (usuarioActual == null) {
        LoginScreen(onLoginSuccess = { usuarioLogueado ->
            usuarioActual = usuarioLogueado
        })
    } else {
        if (usuarioActual!!.esAdmin) {
            AdminDashboardScreen(
                usuario = usuarioActual!!,
                onLogout = { usuarioActual = null }
            )
        } else {
            UserHomeScreen(
                usuario = usuarioActual!!,
                onLogout = { usuarioActual = null }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "PagosYa", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val usuario = SupabaseClient.login(correo, password)
                    isLoading = false

                    if (usuario != null) {
                        Toast.makeText(context, "Acceso concedido", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(usuario)
                    } else {
                        Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Ingresar")
            }
        }
    }
}