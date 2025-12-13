package com.example.serviciocobros.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.serviciocobros.data.model.Usuario
import com.example.serviciocobros.AppTheme
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    usuario: Usuario,
    onLogout: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    // 0 = Admin, 1 = Cliente, 2 = Perfil
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(when(selectedTab) {
                        0 -> "PagosYa (Admin)"
                        1 -> "Vista Cliente"
                        else -> "Mi Perfil"
                    })
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    label = { Text("Admin") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Cliente") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text("Perfil") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> AdminHomeScreen(usuario = usuario)
                1 -> UserHomeContent(usuario = usuario, onVerMenu = {})
                2 -> ProfileScreen(
                    usuario = usuario,
                    onLogout = onLogout,
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}