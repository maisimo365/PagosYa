package com.example.serviciocobros.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.serviciocobros.data.SupabaseClient
import com.example.serviciocobros.data.model.DeudaInsert
import com.example.serviciocobros.data.model.Plato
import com.example.serviciocobros.data.model.Usuario
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    var filtroEmpresa by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedCliente by remember { mutableStateOf<Usuario?>(null) }
    var selectedPlato by remember { mutableStateOf<Plato?>(null) }

    var esConsumoExtra by remember { mutableStateOf(false) }
    var descripcion by remember { mutableStateOf("") }
    var montoManual by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }

    fun cargarDatos() {
        scope.launch {
            val clientesFrescos = SupabaseClient.obtenerClientes()
            val platosFrescos = SupabaseClient.obtenerPlatos()

            todosLosClientes = clientesFrescos
            platos = platosFrescos

            if (filtroEmpresa == null) {
                val primeraEmpresa = clientesFrescos
                    .mapNotNull { it.empresa }
                    .filter { it != "Administrador" }
                    .firstOrNull()
                if (primeraEmpresa != null) filtroEmpresa = primeraEmpresa
            }

            isLoadingDatos = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    val empresasDisponibles = remember(todosLosClientes) {
        todosLosClientes
            .mapNotNull { it.empresa }
            .filter { it != "Administrador" && it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val clientesFiltrados = remember(todosLosClientes, filtroEmpresa, searchQuery) {
        todosLosClientes.filter { cliente ->
            val coincideEmpresa = filtroEmpresa == null || cliente.empresa.equals(filtroEmpresa, ignoreCase = true)
            val coincideBusqueda = cliente.nombre.contains(searchQuery, ignoreCase = true)
            coincideEmpresa && coincideBusqueda
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
            onRefresh = { isRefreshing = true; cargarDatos() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoadingDatos && !isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; selectedCliente = null },
                            label = { Text("Buscar cliente...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(empresasDisponibles) { empresa ->
                                FilterChip(
                                    selected = filtroEmpresa == empresa,
                                    onClick = {
                                        filtroEmpresa = empresa
                                        selectedCliente = null
                                        searchQuery = ""
                                    },
                                    label = { Text(empresa) },
                                    leadingIcon = if (filtroEmpresa == empresa) {
                                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        Text(
                            "Seleccionar Cliente (${clientesFiltrados.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (clientesFiltrados.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                Text("No hay clientes con ese nombre", color = Color.Gray)
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(clientesFiltrados) { cliente ->
                                    ClienteCard(
                                        cliente = cliente,
                                        isSelected = selectedCliente?.id == cliente.id,
                                        onClick = {
                                            selectedCliente = if (selectedCliente?.id == cliente.id) null else cliente
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (esConsumoExtra) "Consumo Externo" else "Menú Disponible",
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

                        if (!esConsumoExtra) {
                            SimpleVerticalGrid(items = platos, columns = 2) { plato ->
                                PlatoCard(
                                    plato = plato,
                                    isSelected = selectedPlato?.id == plato.id,
                                    onClick = {
                                        selectedPlato = if (selectedPlato?.id == plato.id) null else plato
                                    }
                                )
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = descripcion,
                                        onValueChange = {
                                            if (it.length <= 30) descripcion = it
                                        },
                                        label = { Text("Descripción") },
                                        placeholder = { Text("Ej: Gaseosa 3L...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = {
                                            Text(
                                                text = "${descripcion.length}/30",
                                                color = if (descripcion.length == 30) MaterialTheme.colorScheme.error else Color.Gray
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = montoManual,
                                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) montoManual = it },
                                        label = { Text("Monto (Bs)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        prefix = { Text("Bs. ") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (selectedCliente == null) {
                                    Toast.makeText(context, "¡Debes seleccionar un cliente!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val precioFinal: Double
                                val idPlatoFinal: Long?
                                val descFinal: String?

                                if (esConsumoExtra) {
                                    if (descripcion.isBlank() || montoManual.isBlank()) {
                                        Toast.makeText(context, "Descripción y monto obligatorios", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    precioFinal = montoManual.toDoubleOrNull() ?: 0.0
                                    idPlatoFinal = null
                                    descFinal = descripcion
                                } else {
                                    if (selectedPlato == null) {
                                        Toast.makeText(context, "¡Selecciona un plato del menú!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    precioFinal = selectedPlato!!.precio
                                    idPlatoFinal = selectedPlato!!.id
                                    descFinal = null
                                }

                                isSaving = true
                                scope.launch {
                                    val fechaActual = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                                    val nuevaDeuda = DeudaInsert(
                                        idRegistrador = idRegistrador,
                                        idConsumidor = selectedCliente!!.id,
                                        idPlato = idPlatoFinal,
                                        monto = precioFinal,
                                        saldoPendiente = precioFinal,
                                        descripcion = descFinal,
                                        precioPlatoMoment = precioFinal,
                                        fechaConsumo = fechaActual
                                    )

                                    val exito = SupabaseClient.registrarDeuda(nuevaDeuda)
                                    isSaving = false

                                    if (exito) {
                                        Toast.makeText(context, "Pedido anotado con éxito", Toast.LENGTH_SHORT).show()
                                        onSuccess()
                                    } else {
                                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CONFIRMAR PEDIDO")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ClienteCard(cliente: Usuario, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = if(isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = cliente.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = cliente.empresa ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlatoCard(plato: Plato, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 3.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!plato.fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = plato.fotoUrl,
                    contentDescription = plato.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Box(modifier = Modifier.fillMaxSize().border(borderWidth, borderColor, RoundedCornerShape(16.dp)))

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(text = plato.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Bs. ${plato.precio}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD700))
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape).padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun <T> SimpleVerticalGrid(items: List<T>, columns: Int, content: @Composable (T) -> Unit) {
    items.chunked(columns).forEach { rowItems ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            rowItems.forEach { item -> Box(modifier = Modifier.weight(1f)) { content(item) } }
            repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}