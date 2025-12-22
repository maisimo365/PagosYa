package com.example.serviciocobros.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Deuda
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val GreenIncome = Color(0xFF27AE60)
val RedPending = Color(0xFFEB5757)
val BlueSales = Color(0xFF2F80ED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isMonthlyView by remember { mutableStateOf(false) }
    var deudas by remember { mutableStateOf<List<Deuda>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun cargarDatos() {
        scope.launch {
            isLoading = true
            val start: String
            val end: String

            if (isMonthlyView) {
                val yearMonth = YearMonth.from(selectedDate)
                start = yearMonth.atDay(1).toString()
                end = yearMonth.atEndOfMonth().toString()
            } else {
                start = selectedDate.toString()
                end = selectedDate.toString()
            }

            deudas = SupabaseClient.obtenerReporteDeudas(start, end)
            isLoading = false
        }
    }

    LaunchedEffect(selectedDate, isMonthlyView) {
        cargarDatos()
    }

    val totalVendido = deudas.sumOf { it.monto }
    val totalPendiente = deudas.sumOf { it.saldoPendiente }
    val totalCobrado = totalVendido - totalPendiente

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte de Ingresos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = !isMonthlyView,
                            onClick = { isMonthlyView = false },
                            label = { Text("Diario") }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        FilterChip(
                            selected = isMonthlyView,
                            onClick = { isMonthlyView = true },
                            label = { Text("Mensual") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            selectedDate = if (isMonthlyView) selectedDate.minusMonths(1L) else selectedDate.minusDays(1L)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior")
                        }

                        Text(
                            text = if (isMonthlyView) {
                                selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))).uppercase()
                            } else {
                                selectedDate.format(DateTimeFormatter.ofPattern("EEEE dd, MMM", Locale("es", "ES"))).uppercase()
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = {
                            selectedDate = if (isMonthlyView) selectedDate.plusMonths(1L) else selectedDate.plusDays(1L)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente")
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResumenCard(
                        title = "Vendido",
                        amount = totalVendido,
                        color = BlueSales,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        title = "Cobrado",
                        amount = totalCobrado,
                        color = GreenIncome,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (totalPendiente > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = RedPending.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pendiente por cobrar:", color = RedPending, fontWeight = FontWeight.Bold)
                            Text("Bs. ${String.format("%.2f", totalPendiente)}", color = RedPending, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Detalle de Movimientos", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)

                if (deudas.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hubo movimientos en esta fecha.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(deudas) { deuda ->
                            ReporteItem(deuda)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = color)
            Text(
                text = "Bs. ${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ReporteItem(deuda: Deuda) {
    val pagado = deuda.monto - deuda.saldoPendiente
    val esPagadoCompleto = deuda.saldoPendiente == 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deuda.descripcion ?: "Sin descripción",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (deuda.fecha.length >= 10) deuda.fecha.take(10) else deuda.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Venta: ${deuda.monto}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (esPagadoCompleto) {
                    Text("Cobrado: ${deuda.monto}", color = GreenIncome, fontWeight = FontWeight.Bold)
                } else {
                    Text("Cobrado: $pagado", color = Color.Gray, fontSize = 12.sp)
                    Text("Debe: ${deuda.saldoPendiente}", color = RedPending, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}