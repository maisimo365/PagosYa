package com.example.serviciocobros

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import com.example.serviciocobros.ui.cobrar.CobrarDetailScreen
import com.example.serviciocobros.ui.cobrar.CobrarUsersScreen
import com.example.serviciocobros.ui.debts.UserDebtsScreen
import com.example.serviciocobros.ui.home.AddDishScreen
import com.example.serviciocobros.ui.home.AddUserScreen
import com.example.serviciocobros.ui.home.AdminDashboardScreen
import com.example.serviciocobros.ui.home.ManageUsersScreen
import com.example.serviciocobros.ui.home.PaymentHistoryScreen
import com.example.serviciocobros.ui.home.RegisterDebtScreen
import com.example.serviciocobros.ui.home.UserHomeScreen
import com.example.serviciocobros.ui.menu.EditDishScreen
import com.example.serviciocobros.ui.menu.EditMenuScreen
import com.example.serviciocobros.ui.menu.MenuScreen
import com.example.serviciocobros.ui.theme.ServicioCobrosTheme
import kotlinx.coroutines.launch

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
                    AppNavigation(currentTheme, onThemeChange, prefs)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    prefs: android.content.SharedPreferences
) {
    var usuarioActual by rememberSaveable { mutableStateOf<Usuario?>(null) }
    var pantallaSecundaria by rememberSaveable { mutableStateOf<String?>(null) }
    var adminSelectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedUserName by rememberSaveable { mutableStateOf<String?>(null) }
    var platoAEditar by remember { mutableStateOf<Plato?>(null) }

    var isCheckingSession by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedId = prefs.getLong("saved_user_id", -1L)
        if (savedId != -1L) {
            val user = SupabaseClient.obtenerUsuarioPorId(savedId)
            if (user != null && user.activo) {
                usuarioActual = user
            }
        }
        isCheckingSession = false
    }

    val refreshUser: suspend () -> Unit = {
        val id = usuarioActual?.id
        if (id != null) {
            val actualizado = SupabaseClient.obtenerUsuarioPorId(id)
            if (actualizado != null) usuarioActual = actualizado
        }
    }

    val logoutAction: () -> Unit = {
        usuarioActual = null
        prefs.edit().remove("saved_user_id").apply()
        pantallaSecundaria = null
        adminSelectedTab = 0
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeTerracotta)
        }
    } else if (usuarioActual == null) {
        LoginScreen(
            onLoginSuccess = { usuario ->
                prefs.edit().putLong("saved_user_id", usuario.id).apply()
                usuarioActual = usuario
            }
        )
    } else {

        BackHandler(enabled = pantallaSecundaria != null) {
            if (pantallaSecundaria == "editar_detalle_plato") {
                pantallaSecundaria = "editar_plato"
            } else {
                pantallaSecundaria = null
            }
        }

        when {
            pantallaSecundaria == "menu" -> {
                MenuScreen(onBack = { pantallaSecundaria = null })
            }
            pantallaSecundaria == "mis_deudas" -> {
                UserDebtsScreen(
                    userId = usuarioActual!!.id,
                    onBack = { pantallaSecundaria = null }
                )
            }
            pantallaSecundaria == "historial_pagos" -> {
                PaymentHistoryScreen(
                    userId = usuarioActual!!.id,
                    onBack = { pantallaSecundaria = null }
                )
            }

            pantallaSecundaria == "agregar_plato" -> {
                AddDishScreen(
                    onBack = { pantallaSecundaria = null },
                    onDishAdded = {
                        pantallaSecundaria = null
                    }
                )
            }

            pantallaSecundaria == "agregar_usuario" -> {
                AddUserScreen(
                    onBack = { pantallaSecundaria = null },
                    onUserAdded = {
                        pantallaSecundaria = null
                    }
                )
            }

            pantallaSecundaria == "desactivar_usuario" -> {
                ManageUsersScreen(
                    onBack = { pantallaSecundaria = null }
                )
            }

            pantallaSecundaria == "editar_plato" -> {
                EditMenuScreen(
                    onBack = { pantallaSecundaria = null },
                    onEditClick = { plato ->
                        platoAEditar = plato
                        pantallaSecundaria = "editar_detalle_plato"
                    }
                )
            }

            pantallaSecundaria == "editar_detalle_plato" && platoAEditar != null -> {
                EditDishScreen(
                    plato = platoAEditar!!,
                    onBack = { pantallaSecundaria = "editar_plato" },
                    onSuccess = {
                        pantallaSecundaria = "editar_plato"
                    }
                )
            }

            pantallaSecundaria == "anotar" -> {
                RegisterDebtScreen(
                    idRegistrador = usuarioActual!!.id,
                    onBack = { pantallaSecundaria = null },
                    onSuccess = { pantallaSecundaria = null }
                )
            }

            pantallaSecundaria == "cobrar" -> {
                CobrarUsersScreen(
                    onBack = { pantallaSecundaria = null },
                    onUserSelected = { usuario ->
                        selectedUserId = usuario.id
                        selectedUserName = usuario.nombre
                        pantallaSecundaria = "cobrar_detail"
                    }
                )
            }

            pantallaSecundaria == "cobrar_detail" && selectedUserId != null -> {
                BackHandler(enabled = true) {
                    pantallaSecundaria = "cobrar"
                }
                CobrarDetailScreen(
                    userId = selectedUserId!!,
                    userName = selectedUserName!!,
                    onBack = { pantallaSecundaria = "cobrar" }
                )
            }

            else -> {
                if (usuarioActual!!.esAdmin) {
                    AdminDashboardScreen(
                        usuario = usuarioActual!!,
                        onLogout = logoutAction,
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        onRefresh = refreshUser,
                        onNavigateToAnotar = { pantallaSecundaria = "anotar" },
                        onNavigateToCobrar = { pantallaSecundaria = "cobrar" },
                        onNavigateToAddPlato = { pantallaSecundaria = "agregar_plato" },
                        onNavigateToEditPlato = { pantallaSecundaria = "editar_plato" },
                        onNavigateToAddUser = { pantallaSecundaria = "agregar_usuario" },
                        onNavigateToDeactivateUser = { pantallaSecundaria = "desactivar_usuario" },
                        selectedTab = adminSelectedTab,
                        onTabSelected = { adminSelectedTab = it }
                    )
                } else {
                    UserHomeScreen(
                        usuario = usuarioActual!!,
                        onLogout = logoutAction,
                        onVerMenu = { pantallaSecundaria = "menu" },
                        onVerDeudas = { pantallaSecundaria = "mis_deudas" },
                        onVerHistorial = { pantallaSecundaria = "historial_pagos" },
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        onRefresh = refreshUser
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
        Image(
            painter = painterResource(id = R.drawable.logo_pagosya),
            contentDescription = "Logo PagosYa",
            modifier = Modifier.width(140.dp).heightIn(max = 120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append("Pagos") }
                withStyle(style = SpanStyle(color = GreenEmerald)) { append("Ya") }
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Text(text = "Tu sistema de cobros", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeTerracotta, focusedLabelColor = OrangeTerracotta, cursorColor = OrangeTerracotta),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangeTerracotta, focusedLabelColor = OrangeTerracotta, cursorColor = OrangeTerracotta),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) { Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show(); return@Button }
                isLoading = true
                scope.launch {
                    val usuario = SupabaseClient.login(correo, password)
                    isLoading = false
                    if (usuario != null) {
                        Toast.makeText(context, "Bienvenido: ${usuario.nombre}", Toast.LENGTH_LONG).show()
                        onLoginSuccess(usuario)
                    }
                    else { Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show() }
                }
            },
            enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeTerracotta, contentColor = Color.White)
        ) {
            if (isLoading) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) } else { Text("Ingresar", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}