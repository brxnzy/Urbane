package com.example.urbane.ui.admin.users.view

import android.R.attr.padding
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.urbane.ui.admin.users.viewmodel.UsersDetailViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urbane.R
import com.example.urbane.data.model.User
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
import com.example.urbane.ui.admin.users.model.UsersIntent
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.utils.getRoleLabelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(userId: String, viewmodel: UsersDetailViewModel,loginViewModel: LoginViewModel, goBack:()-> Unit) {
    val state by viewmodel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewmodel.loadUser(userId)
    }

    Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if(!state.isLoading) stringResource(R.string.perfil_de) + " " + state.user?.name else "",
                            style = MaterialTheme.typography.displayMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.atras),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
            ) { paddingValues ->


    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.errorMessage}")
            }
        }

        state.user != null -> {
            UserDetail(
                user = state.user!!,
                modifier = Modifier.padding(paddingValues),
                viewmodel,
                loginViewModel

            )
        }
    }
    }
}


@Composable
fun UserDetail(
    user: User,
    modifier: Modifier = Modifier,
    viewmodel: UsersDetailViewModel,
    loginViewModel: LoginViewModel
) {
    val state by viewmodel.state.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (user.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(210.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        if (user.email != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        InfoSection {
            UserInfoItem(
                label = "Cédula",
                value = user.idCard ?: "No disponible"
            )

            UserInfoItem(
                label = stringResource(R.string.rol),
                value = stringResource(getRoleLabelRes(user.role_name))
            )

            UserInfoItem(
                label = "Estado",
                value = if (user.active == true) "Activo" else "Inactivo",
                valueColor = if (user.active == true)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Red
            )

            if (user.role_name == "resident") {
                UserInfoItem(
                    label = stringResource(R.string.residencia),
                    value = user.residence_name ?: "No disponible"
                )
                UserInfoItem(
                    label = "Tipo",
                    value = user.residence_type ?: "No disponible"
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        if (currentUser?.userId != user.id) {

            Button(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {

                if (true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar", style = MaterialTheme.typography.titleMedium)
                    }
                } else {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (user.active == true){
                Button(
                    onClick = { viewmodel.processIntent(UsersDetailIntent.DisableUser) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {


                    if (true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deshabilitar", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {

                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
        }else{
            Button(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {


                if (true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar", style = MaterialTheme.typography.titleMedium)
                    }
                } else {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }




        }



        }

    }
}

@Composable
fun InfoSection(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun UserInfoItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

