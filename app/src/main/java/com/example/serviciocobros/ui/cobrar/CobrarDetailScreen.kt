package com.example.serviciocobros.ui.cobrar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.Deuda
import kotlinx.coroutines.launch

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
                    Text("Este cliente no tiene deudas pendientes.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deudas) { deuda ->
                        CobrarDeudaItem(deuda)
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
                        Text("Ingrese los montos manualmente.", style = MaterialTheme.typography.bodySmall)

                        OutlinedTextField(
                            value = efectivoRecibidoInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                                    efectivoRecibidoInput = input
                                }
                            },
                            label = { Text("Efectivo Recibido (Bs.)") },
                            placeholder = { Text("Ej: 200") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = montoPagarInput,
                            onValueChange = { input ->
                                if (input.all { char -> char.isDigit() || char == '.' } && input.count { it == '.' } <= 1) {
                                    montoPagarInput = input
                                }
                            },
                            label = { Text("Monto a Cobrar (Bs.)") },
                            placeholder = { Text("Monto a cobrar de la deuda") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (cambio >= 0 && efectivoRecibido >= montoPagar && montoPagar > 0)
                                    Color(0xFFE8F5E9)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CAMBIO A DAR:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bs. ${String.format("%.2f", cambio)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (cambio >= 0 && efectivoRecibido >= montoPagar && montoPagar > 0)
                                        Color(0xFF2E7D32)
                                    else
                                        Color.Gray
                                )
                            }
                        }

                        if (montoPagar > totalDeuda) {
                            Text(
                                "El monto excede la deuda total (Bs. $totalDeuda)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (montoPagar > efectivoRecibido && efectivoRecibido > 0) {
                            Text(
                                "Falta efectivo para cubrir el monto.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
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
                                    snackbarHostState.showSnackbar("Error al procesar el pago")
                                }
                                isProcesandoPago = false
                            }
                        },
                        enabled = !isProcesandoPago && puedePagar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isProcesandoPago) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Confirmar Pago")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPaymentDialog = false },
                        enabled = !isProcesandoPago
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun CobrarDeudaItem(deuda: Deuda) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!deuda.descripcion.isNullOrBlank()) {
                    Text(text = deuda.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    text = deuda.fecha.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Bs. ${deuda.saldoPendiente}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (deuda.saldoPendiente > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
            )
        }
    }
}