package com.example.serviciocobros.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    usuario: Usuario,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PagosYa") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, ${usuario.nombre}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Panel de Cliente",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            MenuOptionCard(
                titulo = "Mis Deudas",
                descripcion = "Revisa tu saldo pendiente",
                icono = Icons.Default.AccountBalanceWallet,
                colorIcono = Color(0xFFD32F2F),
                onClick = { /* TODO: Navegar a Deudas */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuOptionCard(
                titulo = "Pagos Realizados",
                descripcion = "Ver historial de transacciones",
                icono = Icons.Default.History,
                colorIcono = Color(0xFF1976D2),
                onClick = { /* TODO: Navegar a Historial */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuOptionCard(
                titulo = "Ver Menú",
                descripcion = "Explora nuestros platillos",
                icono = Icons.Default.RestaurantMenu,
                colorIcono = Color(0xFF388E3C),
                onClick = { /* TODO: Navegar a Menú */ }
            )
        }
    }
}

@Composable
fun MenuOptionCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}