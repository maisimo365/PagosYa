package com.example.serviciocobros.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.PagoHistorico
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.compose.BackHandler

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    userId: Long,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    var pagos by remember { mutableStateOf<List<PagoHistorico>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    fun cargarHistorial() {
        scope.launch {
            pagos = SupabaseClient.obtenerHistorialPagos(userId)
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        cargarHistorial()
    }

    val pagosPorDia = remember(pagos) {
        if (pagos.isEmpty()) emptyMap()
        else {
            pagos.groupBy { pago ->
                try {
                    ZonedDateTime.parse(pago.fechaPago).toLocalDate()
                } catch (e: Exception) {
                    LocalDate.now()
                }
            }.toSortedMap(compareByDescending { it })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pagos") },
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
            onRefresh = { isRefreshing = true; cargarHistorial() }
        ) {
            if (isLoading && !isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pagos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tienes pagos registrados aún.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pagosPorDia.forEach { (diaPago, listaPagos) ->
                        item {
                            HistorialDiaCard(diaPago, listaPagos)
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistorialDiaCard(diaPago: LocalDate, listaPagos: List<PagoHistorico>) {
    val formateadorFecha = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
    val formateadorConsumo = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    val totalDia = listaPagos.sumOf { it.montoPagado }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pagado el:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = diaPago.format(formateadorFecha).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Pagado",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Bs. ${String.format("%.2f", totalDia)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            listaPagos.forEachIndexed { index, pago ->
                val plato = pago.deudas?.platos
                val fechaConsumoStr = try {
                    val fecha = ZonedDateTime.parse(pago.deudas?.fechaConsumo ?: "").toLocalDate()
                    fecha.format(formateadorConsumo)
                } catch (e: Exception) { "" }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (plato?.foto != null) {
                        AsyncImage(
                            model = plato.foto,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plato?.nombre ?: "Pago de deuda",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (fechaConsumoStr.isNotEmpty()) {
                            Text(
                                text = "Consumido el $fechaConsumoStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Bs. ${String.format("%.2f", pago.montoPagado)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        maxLines = 1
                    )
                }

                if (index < listaPagos.size - 1) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
                }
            }
        }
    }
}