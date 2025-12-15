package com.example.serviciocobros.ui.cobrar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun CobrarDetailScreen(
    userId: Long,
    userName: String,
    onBack: () -> Unit
) {
    val cobradorId = 1L

    var deudas by remember { mutableStateOf<List<Deuda>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var isProcesandoPago by remember { mutableStateOf(false) }
    var montoPagarInput by remember { mutableStateOf("") }
    var efectivoRecibidoInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val montoPagar = montoPagarInput.toDoubleOrNull() ?: 0.0
    val efectivoRecibido = efectivoRecibidoInput.toDoubleOrNull() ?: 0.0
    val cambio = if (efectivoRecibido >= montoPagar) efectivoRecibido - montoPagar else 0.0
    val puedePagar = montoPagar > 0 && montoPagar <= totalDeuda && efectivoRecibido >= montoPagar

    fun cargarDeudas() {
        scope.launch {
            isLoading = true
            deudas = SupabaseClient.obtenerMisDeudas(userId)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarDeudas()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cobrar a", style = MaterialTheme.typography.labelSmall)
                        Text(userName, style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isLoading && totalDeuda > 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        montoPagarInput = ""
                        efectivoRecibidoInput = ""
                        showPaymentDialog = true
                    },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    text = { Text("COBRAR") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Adeudado:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Bs. ${String.format("%.2f", totalDeuda)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (deudas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin deudas pendientes.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    deudasPorSemana.forEach { (lunesInicio, listaDeudasSemana) ->
                        item {
                            SemanaCard(lunesInicio, listaDeudasSemana)
                        }
                    }
                }
            }
        }

        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { if (!isProcesandoPago) showPaymentDialog = false },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                title = { Text("Registrar Pago") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Ingrese montos manualmente.", style = MaterialTheme.typography.bodySmall)

                        OutlinedTextField(
                            value = efectivoRecibidoInput,
                            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) efectivoRecibidoInput = input },
                            label = { Text("Efectivo Recibido") },
                            placeholder = { Text("Ej: 200") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = montoPagarInput,
                            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) montoPagarInput = input },
                            label = { Text("Monto a cobrar") },
                            placeholder = { Text("Ej: 150") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (cambio >= 0 && efectivoRecibido >= montoPagar && montoPagar > 0) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CAMBIO:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Bs. ${String.format("%.2f", cambio)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (cambio >= 0 && efectivoRecibido >= montoPagar && montoPagar > 0) Color(0xFF2E7D32) else Color.Gray
                                )
                            }
                        }

                        if (montoPagar > totalDeuda) Text("Excede la deuda total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        if (montoPagar > efectivoRecibido && efectivoRecibido > 0) Text("Falta efectivo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isProcesandoPago = true
                                val exito = SupabaseClient.registrarPago(userId, cobradorId, montoPagar)
                                if (exito) {
                                    snackbarHostState.showSnackbar("Pago registrado. Cambio: Bs. ${String.format("%.2f", cambio)}")
                                    showPaymentDialog = false
                                    cargarDeudas()
                                } else {
                                    snackbarHostState.showSnackbar("Error al procesar pago")
                                }
                                isProcesandoPago = false
                            }
                        },
                        enabled = !isProcesandoPago && puedePagar
                    ) {
                        if (isProcesandoPago) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) else Text("Confirmar")
                    }
                },
                dismissButton = { TextButton(onClick = { showPaymentDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SemanaCard(lunesInicio: LocalDate, deudasSemana: List<Deuda>) {
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                        modifier = Modifier.width(90.dp).padding(end = 4.dp)
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
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
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
                                                text = " | Original: Bs. ${String.format("%.2f", deuda.monto)}",
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
                                        color = MaterialTheme.colorScheme.error
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