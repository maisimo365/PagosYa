package com.example.serviciocobros.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.AppTheme
import com.example.serviciocobros.data.model.Usuario
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.WbSunny

@Composable
fun ProfileScreen(
    usuario: Usuario,
    onLogout: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { newTheme ->
                onThemeChange(newTheme)
                showThemeDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Avatar ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = usuario.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = if (usuario.esAdmin) "Administrador" else "Cliente", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Default.Email, label = "Correo", value = usuario.correo)
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileInfoRow(icon = Icons.Default.Business, label = "Detalle", value = usuario.empresa ?: "Sin empresa")
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "Celular",
                    value = usuario.celular ?: "No registrado"
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                ProfileInfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Miembro desde",
                    value = usuario.fechaRegistro?.take(10) ?: "Desconocido")

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Brightness6, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Tema", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(
                            text = when(currentTheme) {
                                AppTheme.LIGHT -> "Claro"
                                AppTheme.DARK -> "Oscuro"
                                AppTheme.SYSTEM -> "Predeterminado del sistema"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        // Forma redondeada suave (CornerSize 16dp sugerido en PDF)
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOptionItem(
                    title = "Modo Claro",
                    icon = Icons.Default.WbSunny,
                    isSelected = currentTheme == AppTheme.LIGHT,
                    onClick = { onThemeSelected(AppTheme.LIGHT) }
                )
                ThemeOptionItem(
                    title = "Modo Oscuro",
                    icon = Icons.Default.NightsStay,
                    isSelected = currentTheme == AppTheme.DARK,
                    onClick = { onThemeSelected(AppTheme.DARK) }
                )
                ThemeOptionItem(
                    title = "Sistema",
                    icon = Icons.Default.SettingsSuggest,
                    isSelected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onThemeSelected(AppTheme.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ThemeOptionItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Definimos colores según si está seleccionado o no
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}