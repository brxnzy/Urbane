package com.example.urbane.ui.admin.residences.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.urbane.R
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.urbane.ui.admin.residences.view.components.ResidenceCard
import com.example.urbane.ui.admin.users.view.components.UsuarioCardSkeleton


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResidencesScreen(
    viewModel: ResidencesViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    showResidenceDeletedMessage: Boolean = false
) {
    val state by viewModel.state.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }
    var estadoFiltro by remember { mutableStateOf<String?>(null) }
    var filtroMenuExpandido by remember { mutableStateOf(false) }
    val tiposPropiedad = listOf("Apartamento", "Casa", "Villa", "Terreno", "Local")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadResidences()
    }


    LaunchedEffect(showResidenceDeletedMessage) {
        if (showResidenceDeletedMessage) {
            snackbarHostState.showSnackbar(
                message = "Residencia eliminada exitosamente",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    val residenciasFiltradas = state.residences.filter { residence ->

        val coincideNombre =
            residence.name.contains(busqueda, ignoreCase = true)

        val coincideTipo =
            tipoFiltro?.let { selected -> residence.type.equals(selected, true) } ?: true

        val coincideEstado = when (estadoFiltro) {
            "Disponible" -> residence.available
            "Ocupada" -> !residence.available
            else -> true
        }

        coincideNombre && coincideTipo && coincideEstado
    }



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_RESIDENCES_ADD) },
                modifier = Modifier// subimos el FAB sin afectar layout
                .offset(y = 0.dp),
            containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = null)
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .offset(y = 140.dp)
                    .padding(bottom = 16.dp)
            )
        }
    ) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // BUSCADOR + FILTRO
            Box {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = { stringResource(R.string.buscar_por_nombre) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { filtroMenuExpandido = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = filtroMenuExpandido,
                    onDismissRequest = { filtroMenuExpandido = false }
                ) {

                    Text(
                        "Tipo de propiedad",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )

                    tiposPropiedad.forEach { tipo ->
                        DropdownMenuItem(
                            onClick = {
                                tipoFiltro = if (tipoFiltro == tipo) null else tipo
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(tipo)
                                    Checkbox(
                                        checked = tipoFiltro == tipo,
                                        onCheckedChange = {
                                            tipoFiltro = if (it) tipo else null
                                        }
                                    )
                                }
                            }
                        )
                    }

                    Divider()

                    Text(
                        "Estado",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )

                    DropdownMenuItem(
                        onClick = {
                            estadoFiltro = if (estadoFiltro == "Disponible") null else "Disponible"
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Disponible", modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = estadoFiltro == "Disponible",
                                    onCheckedChange = {
                                        estadoFiltro = if (it) "Disponible" else null
                                    }
                                )
                            }
                        }
                    )

                    DropdownMenuItem(
                        onClick = {
                            estadoFiltro = if (estadoFiltro == "Ocupada") null else "Ocupada"
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ocupada", modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = estadoFiltro == "Ocupada",
                                    onCheckedChange = {
                                        estadoFiltro = if (it) "Ocupada" else null
                                    }
                                )
                            }
                        }
                    )

                    Divider()

                    DropdownMenuItem(
                        onClick = {
                            tipoFiltro = null
                            estadoFiltro = null
                        },
                        text = { Text("Limpiar filtros") }
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(5) {
                            UsuarioCardSkeleton()
                        }
                    }
                }

                residenciasFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se encontraron residencias")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(residenciasFiltradas.size) { index ->
                            val residence = residenciasFiltradas[index]
                            ResidenceCard(
                                residence = residence,
                                onClick = {
                                    navController.navigate(
                                        Routes.ADMIN_RESIDENCES_DETAIL.replace(
                                            "{id}",
                                            residence.id.toString()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
