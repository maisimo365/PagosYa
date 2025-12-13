package com.example.serviciocobros

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Usuario
import com.example.serviciocobros.ui.home.AdminDashboardScreen
import com.example.serviciocobros.ui.home.UserHomeScreen
import com.example.serviciocobros.ui.menu.MenuScreen
import com.example.serviciocobros.ui.theme.ServicioCobrosTheme
import kotlinx.coroutines.launch

// Definimos tus colores de marca
val OrangeTerracotta = Color(0xFFF2994A)
val GreenEmerald = Color(0xFF27AE60)

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

    // Estado para el scroll
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
            .verticalScroll(scrollState)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_pagosya1),
            contentDescription = "Logo PagosYa",
            modifier = Modifier
                .width(140.dp)
                .heightIn(max = 120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Texto con estilos mixtos
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append("Pagos")
                }
                withStyle(style = SpanStyle(color = GreenEmerald)) { // "Ya" en Verde
                    append("Ya")
                }
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Tu sistema de cobros",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo Correo
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeTerracotta,
                focusedLabelColor = OrangeTerracotta,
                cursorColor = OrangeTerracotta
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangeTerracotta,
                focusedLabelColor = OrangeTerracotta,
                cursorColor = OrangeTerracotta
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val usuario = SupabaseClient.login(correo, password)
                    isLoading = false

                    if (usuario != null) {
                        Toast.makeText(context, "Bienvenido: ${usuario.nombre}", Toast.LENGTH_LONG).show()
                        onLoginSuccess(usuario)
                    } else {
                        Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeTerracotta,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Ingresar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}