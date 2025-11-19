package com.example.urbane.ui.admin.users.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.urbane.R
import com.example.urbane.data.model.User
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.ui.auth.viewmodel.LoginViewModel


@SuppressLint("SuspiciousIndentation", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UsersScreen(viewmodel: UsersViewModel,modifier: Modifier = Modifier, navController: NavController) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var busqueda by remember { mutableStateOf("") }
    val state by viewmodel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        snapshotFlow { navBackStackEntry?.lifecycle?.currentState }
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    viewmodel.loadUsers()
                }
            }
    }


    LaunchedEffect(Unit) {
        viewmodel.loadUsers()
    }


    val filtros = listOf("Todos", "Administrador", "Residente","Deshabilitados")

    val usuariosFiltrados = state.users.filter { user ->
        val cumpleBusqueda = user.name.contains(busqueda, ignoreCase = true)

        val cumpleFiltro = when (filtroSeleccionado) {
            "Todos" -> user.active == true
            "Deshabilitados" -> user.active == false
            "Administrador" -> user.role_name.equals("admin", ignoreCase = true) && user.active == true
            "Residente" -> user.role_name.equals("resident", ignoreCase = true) && user.active == true
            else -> true
        }

        cumpleBusqueda && cumpleFiltro
    }



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADMIN_USERS_ADD) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, tint = Color.White, contentDescription = "Agregar usuario")
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()

        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                placeholder = { Text(stringResource(R.string.buscar_por_nombre), color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                singleLine = true
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = { filtroSeleccionado = filtro },
                        label = { Text(filtro) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading) {
                    // mostrar skeletons mientras carga
                    items(5) {
                        UsuarioCardSkeleton()
                    }
                }

                    items(usuariosFiltrados) { usuario ->
                    UsuarioCard(usuario) {
                        navController.navigate(Routes.ADMIN_USERS_DETAIL.replace("{id}", usuario.id))
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 32.dp)
                            .fillMaxWidth(),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_se_encontraron_m_s_usuarios),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            stringResource(R.string.intenta_ajustar_tu_b_squeda_o_filtros),
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UsuarioCard(
    usuario: User,
    detailUser: ()->Unit

) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = detailUser,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil o ícono por defecto
            if (usuario.photoUrl != null && usuario.photoUrl.isNotEmpty()) {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(usuario.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.foto_de) + " " + usuario.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

            } else {

                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.usuario_sin_foto),
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onTertiary
                    )

            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = usuario.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = usuario.email ?: stringResource(R.string.sin_correo),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.ver_detalles),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}