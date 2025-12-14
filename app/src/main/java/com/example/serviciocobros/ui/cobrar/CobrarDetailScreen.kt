package com.example.serviciocobros.ui.cobrar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
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
    var montoInput by remember { mutableStateOf("") }
    var isProcesandoPago by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val totalDeuda = remember(deudas) { deudas.sumOf { it.saldoPendiente } }

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
                    onClick = { showPaymentDialog = true },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    text = { Text("REGISTRAR PAGO") },
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
                title = { Text("Registrar Pago") },
                text = {
                    Column {
                        Text("Ingrese el monto a pagar. Se descontará automáticamente de las deudas más antiguas.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = montoInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' } && newValue.count { it == '.' } <= 1) {
                                    montoInput = newValue
                                }
                            },
                            label = { Text("Monto (Bs.)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val monto = montoInput.toDoubleOrNull()
                            if (monto != null && monto > 0) {
                                scope.launch {
                                    isProcesandoPago = true
                                    val exito = SupabaseClient.registrarPago(userId, cobradorId, monto)

                                    if (exito) {
                                        snackbarHostState.showSnackbar("Pago registrado con éxito")
                                        montoInput = ""
                                        showPaymentDialog = false
                                        cargarDeudas()
                                    } else {
                                        snackbarHostState.showSnackbar("Error al procesar el pago")
                                    }
                                    isProcesandoPago = false
                                }
                            }
                        },
                        enabled = !isProcesandoPago && montoInput.isNotEmpty()
                    ) {
                        if (isProcesandoPago) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Confirmar")
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