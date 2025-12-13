package com.example.serviciocobros

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.serviciocobros.ui.home.AdminDashboardScreen
import com.example.serviciocobros.ui.home.UserHomeScreen
import com.example.serviciocobros.ui.menu.MenuScreen
import com.example.serviciocobros.ui.theme.ServicioCobrosTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

enum class AppTheme { LIGHT, DARK, SYSTEM }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            val savedThemeName = remember { prefs.getString("theme_mode", AppTheme.SYSTEM.name) }

            var currentTheme by remember { mutableStateOf(AppTheme.valueOf(savedThemeName ?: "SYSTEM")) }

            val onThemeChange: (AppTheme) -> Unit = { newTheme ->
                currentTheme = newTheme
                prefs.edit().putString("theme_mode", newTheme.name).apply()
            }

            val isDarkTheme = when (currentTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            ServicioCobrosTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(currentTheme, onThemeChange)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(currentTheme: AppTheme, onThemeChange: (AppTheme) -> Unit) {
    var usuarioActual by rememberSaveable { mutableStateOf<Usuario?>(null) }
    var pantallaSecundaria by rememberSaveable { mutableStateOf<String?>(null) }

    if (usuarioActual == null) {
        LoginScreen(onLoginSuccess = { usuarioActual = it })
    } else {
        if (pantallaSecundaria == "menu") {
            MenuScreen(onBack = { pantallaSecundaria = null })
        } else {
            if (usuarioActual!!.esAdmin) {
                AdminDashboardScreen(
                    usuario = usuarioActual!!,
                    onLogout = { usuarioActual = null },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            } else {
                UserHomeScreen(
                    usuario = usuarioActual!!,
                    onLogout = { usuarioActual = null },
                    onVerMenu = { pantallaSecundaria = "menu" },
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
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
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "PagosYa", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
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
            } else { Text("Ingresar") }
        }
    }
}