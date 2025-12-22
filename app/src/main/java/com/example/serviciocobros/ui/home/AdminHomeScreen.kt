package com.example.serviciocobros.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.model.Usuario

@Composable
fun AdminHomeScreen(
    usuario: Usuario,
    onAnotarClick: () -> Unit,
    onCobrarClick: () -> Unit,
    onAddPlatoClick: () -> Unit,
    onEditPlatoClick: () -> Unit,
    onAddUserClick: () -> Unit,
    onDeactivateUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panel de Administrador",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hola, Admin ${usuario.nombre}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Acciones Rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminBigButton(
                text = "Anotar",
                icon = Icons.Default.EditNote,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                onClick = onAnotarClick
            )
            AdminBigButton(
                text = "Cobrar",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f),
                onClick = onCobrarClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Gestión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        AdminOptionItem(icon = Icons.Default.Restaurant, title = "Añadir Plato") { onAddPlatoClick() }
        AdminOptionItem(icon = Icons.Default.Edit, title = "Editar Plato") { onEditPlatoClick() }
        AdminOptionItem(icon = Icons.Default.PersonAdd, title = "Añadir Usuario") { onAddUserClick() }
        AdminOptionItem(icon = Icons.Default.Delete, title = "Administar Usuarios") { onDeactivateUserClick() }
    }
}

@Composable
fun AdminBigButton(text: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminOptionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}