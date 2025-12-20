package com.example.serviciocobros.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.serviciocobros.AppTheme
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    usuario: Usuario,
    onLogout: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onRefresh: suspend () -> Unit,
    onNavigateToAnotar: () -> Unit,
    onNavigateToCobrar: () -> Unit,
    onNavigateToAddPlato: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    BackHandler(enabled = selectedTab != 0) {
        onTabSelected(0)
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(when(selectedTab) {
                        0 -> "PagosYa (Admin)"
                        1 -> "Gestión de Menú"
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
                    onClick = { onTabSelected(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
                    label = { Text("Menú") },
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text("Perfil") },
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            state = pullState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    onRefresh()
                    refreshTrigger++
                    isRefreshing = false
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AdminHomeScreen(
                        usuario = usuario,
                        onAnotarClick = onNavigateToAnotar,
                        onCobrarClick = onNavigateToCobrar,
                        onAddPlatoClick = onNavigateToAddPlato
                    )
                    1 -> AdminMenuContent(
                        refreshTrigger = refreshTrigger,
                        onAddPlatoClick = onNavigateToAddPlato
                    )
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
}

@Composable
fun AdminMenuContent(
    refreshTrigger: Int,
    onAddPlatoClick: () -> Unit
) {
    var platos by remember { mutableStateOf<List<Plato>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshTrigger) {
        scope.launch {
            isLoading = true
            platos = SupabaseClient.obtenerPlatos()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && platos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (platos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay platos registrados.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(platos) { plato ->
                        AdminPlatoItem(plato)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPlatoClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Plato")
        }
    }
}

@Composable
fun AdminPlatoItem(plato: Plato) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageModifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
            if (plato.fotoUrl.isNullOrBlank()) {
                Box(modifier = imageModifier.background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White)
                }
            } else {
                AsyncImage(model = plato.fotoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = imageModifier)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = plato.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Bs. ${plato.precio}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF388E3C))
            }
        }
    }
}