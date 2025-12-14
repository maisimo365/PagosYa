package com.example.serviciocobros.ui.debts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Deuda
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDebtsScreen(
    userId: Long,
    onBack: () -> Unit
) {
    var deudas by remember { mutableStateOf<List<Deuda>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    fun cargarDeudas() {
        scope.launch {
            deudas = SupabaseClient.obtenerMisDeudas(userId)
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        cargarDeudas()
    }

    val totalDeuda = remember(deudas) { deudas.sumOf { it.saldoPendiente } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Deudas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(padding).fillMaxSize(),
            state = pullState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                cargarDeudas()
            }
        ) {
            if (isLoading && !isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total a Pagar", color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = "Bs. ${String.format("%.2f", totalDeuda)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (deudas.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("¡Estás al día! No tienes deudas pendientes.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text("Detalle de Consumos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            items(deudas) { deuda ->
                                DeudaItem(deuda)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeudaItem(deuda: Deuda) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deuda.platos?.nombre ?: "Consumo Extra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!deuda.descripcion.isNullOrBlank()) {
                    Text(text = deuda.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Text(
                    text = deuda.fecha.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Bs. ${deuda.saldoPendiente}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}