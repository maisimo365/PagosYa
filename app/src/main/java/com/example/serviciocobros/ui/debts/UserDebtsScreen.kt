package com.example.serviciocobros.ui.debts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Deuda
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
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

    val deudasPorSemana = remember(deudas) {
        if (deudas.isEmpty()) emptyMap()
        else {
            deudas.groupBy { deuda ->
                try {
                    val fecha = ZonedDateTime.parse(deuda.fecha).toLocalDate()
                    fecha.with(DayOfWeek.MONDAY)
                } catch (e: Exception) {
                    LocalDate.now().with(DayOfWeek.MONDAY)
                }
            }.toSortedMap(compareByDescending { it })
        }
    }

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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
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
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total a Pagar", color = Color.White.copy(alpha = 0.9f))
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
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            deudasPorSemana.forEach { (lunesInicio, listaDeudasSemana) ->
                                item {
                                    SemanaCardClient(lunesInicio, listaDeudasSemana)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SemanaCardClient(lunesInicio: LocalDate, deudasSemana: List<Deuda>) {
    val formateadorDia = DateTimeFormatter.ofPattern("EEEE d", Locale("es", "ES"))
    val formateadorTitulo = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    val domingoFin = lunesInicio.plusDays(6)
    val subtotalSemana = deudasSemana.sumOf { it.saldoPendiente }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Semana:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "${lunesInicio.format(formateadorTitulo)} - ${domingoFin.format(formateadorTitulo)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Semana", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "Bs. ${String.format("%.2f", subtotalSemana)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            for (i in 0..6) {
                val diaActual = lunesInicio.plusDays(i.toLong())
                val deudasDelDia = deudasSemana.filter {
                    try {
                        ZonedDateTime.parse(it.fecha).toLocalDate() == diaActual
                    } catch (e: Exception) { false }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = diaActual.format(formateadorDia).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (deudasDelDia.isNotEmpty()) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier
                            .width(90.dp)
                            .padding(end = 4.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        if (deudasDelDia.isEmpty()) {
                            Text("-", color = Color.Gray)
                        } else {
                            deudasDelDia.forEach { deuda ->
                                val nombrePlato = deuda.platos?.nombre
                                val desc = deuda.descripcion
                                val tituloMostrar = when {
                                    !nombrePlato.isNullOrBlank() -> nombrePlato
                                    !desc.isNullOrBlank() -> desc
                                    else -> "Sin detalle"
                                }
                                val esDeudaParcial = deuda.saldoPendiente < deuda.monto

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tituloMostrar,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (esDeudaParcial) {
                                            Text(
                                                text = " | Original= Bs. ${String.format("%.2f", deuda.monto)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 4.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Bs. ${String.format("%.2f", deuda.saldoPendiente)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                if (i < 6) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }
        }
    }
}