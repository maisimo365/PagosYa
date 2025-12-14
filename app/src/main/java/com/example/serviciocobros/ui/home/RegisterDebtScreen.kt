package com.example.serviciocobros.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.DeudaInsert
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDebtScreen(
    idRegistrador: Long,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var todosLosClientes by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var platos by remember { mutableStateOf<List<Plato>>(emptyList()) }
    var isLoadingDatos by remember { mutableStateOf(true) }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    var filtroEmpresa by remember { mutableStateOf("Carpinteria") }
    var esConsumoExtra by remember { mutableStateOf(false) }

    var selectedCliente by remember { mutableStateOf<Usuario?>(null) }
    var expandedCliente by remember { mutableStateOf(false) }

    var selectedPlato by remember { mutableStateOf<Plato?>(null) }
    var expandedPlato by remember { mutableStateOf(false) }

    var descripcion by remember { mutableStateOf("") }
    var montoManual by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    fun cargarDatos() {
        scope.launch {
            val clientesFrescos = SupabaseClient.obtenerClientes()
            val platosFrescos = SupabaseClient.obtenerPlatos()

            todosLosClientes = clientesFrescos
            platos = platosFrescos

            isLoadingDatos = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    val clientesFiltrados = remember(todosLosClientes, filtroEmpresa) {
        todosLosClientes.filter {
            it.empresa?.contains(filtroEmpresa, ignoreCase = true) == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anotar Pedido") },
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
                cargarDatos()
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoadingDatos && !isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Text("Filtrar por Empresa", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipEmpresa(
                                label = "Carpintería",
                                selected = filtroEmpresa == "Carpinteria",
                                onClick = { filtroEmpresa = "Carpinteria"; selectedCliente = null }
                            )
                            FilterChipEmpresa(
                                label = "Supermercado",
                                selected = filtroEmpresa == "Supermercado",
                                onClick = { filtroEmpresa = "Supermercado"; selectedCliente = null }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedCliente,
                            onExpandedChange = { expandedCliente = !expandedCliente }
                        ) {
                            OutlinedTextField(
                                value = selectedCliente?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cliente (${clientesFiltrados.size})") },
                                placeholder = { Text("Selecciona...") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCliente) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCliente,
                                onDismissRequest = { expandedCliente = false }
                            ) {
                                if (clientesFiltrados.isEmpty()) {
                                    DropdownMenuItem(text = { Text("No hay clientes en esta empresa") }, onClick = { expandedCliente = false })
                                } else {
                                    clientesFiltrados.forEach { cliente ->
                                        DropdownMenuItem(
                                            text = { Text(cliente.nombre, fontWeight = FontWeight.Bold) },
                                            onClick = { selectedCliente = cliente; expandedCliente = false }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (esConsumoExtra) "Consumo Externo" else "Consumo del Menú",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (esConsumoExtra) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = esConsumoExtra,
                                onCheckedChange = {
                                    esConsumoExtra = it
                                    selectedPlato = null
                                    montoManual = ""
                                    descripcion = ""
                                },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (esConsumoExtra) Icons.Default.AddShoppingCart else Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        Text(
                            text = if (esConsumoExtra) "Registrar algo que no está en el menú" else "Elegir un plato existente",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!esConsumoExtra) {
                            ExposedDropdownMenuBox(
                                expanded = expandedPlato,
                                onExpandedChange = { expandedPlato = !expandedPlato }
                            ) {
                                OutlinedTextField(
                                    value = selectedPlato?.let { "${it.nombre} - Bs. ${it.precio}" } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Plato") },
                                    placeholder = { Text("Selecciona el plato") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlato) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPlato,
                                    onDismissRequest = { expandedPlato = false }
                                ) {
                                    platos.forEach { plato ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(plato.nombre)
                                                    Text("Bs. ${plato.precio}", fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            onClick = { selectedPlato = plato; expandedPlato = false }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                label = { Text("Descripción (Obligatorio)") },
                                placeholder = { Text("Ej: Gaseosa 3L, Pan extra...") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = descripcion.isBlank()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = montoManual,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) montoManual = it },
                                label = { Text("Monto (Bs)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("Bs. ") }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (selectedCliente == null) {
                                    Toast.makeText(context, "Selecciona un cliente", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val precioFinal: Double
                                val idPlatoFinal: Long?
                                val descFinal: String?

                                if (esConsumoExtra) {
                                    if (descripcion.isBlank() || montoManual.isBlank()) {
                                        Toast.makeText(context, "Descripción y monto son obligatorios", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    precioFinal = montoManual.toDoubleOrNull() ?: 0.0
                                    idPlatoFinal = null
                                    descFinal = descripcion
                                } else {
                                    if (selectedPlato == null) {
                                        Toast.makeText(context, "Selecciona un plato", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    precioFinal = selectedPlato!!.precio
                                    idPlatoFinal = selectedPlato!!.id
                                    descFinal = null
                                }

                                isSaving = true
                                scope.launch {
                                    val nuevaDeuda = DeudaInsert(
                                        idRegistrador = idRegistrador,
                                        idConsumidor = selectedCliente!!.id,
                                        idPlato = idPlatoFinal,
                                        monto = precioFinal,
                                        saldoPendiente = precioFinal,
                                        descripcion = descFinal,
                                        precioPlatoMoment = precioFinal
                                    )

                                    val exito = SupabaseClient.registrarDeuda(nuevaDeuda)
                                    isSaving = false

                                    if (exito) {
                                        Toast.makeText(context, "Anotado correctamente", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                    } else {
                                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirmar Pedido", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipEmpresa(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = label, fontWeight = FontWeight.SemiBold)
        }
    }
}