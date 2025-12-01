package com.example.urbane.ui.admin.users.view

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.urbane.R
import com.example.urbane.data.local.SessionManager
import com.example.urbane.data.model.User
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.model.DetailSuccess
import com.example.urbane.ui.admin.users.model.UsersDetailIntent
import com.example.urbane.ui.admin.users.view.components.DisabledDialog
import com.example.urbane.ui.admin.users.view.components.EditUserDialog
import com.example.urbane.ui.admin.users.view.components.EnableDialog
import com.example.urbane.ui.admin.users.view.components.EnableResidentDialog
import com.example.urbane.ui.admin.users.view.components.InfoSection
import com.example.urbane.ui.admin.users.view.components.UserInfoItem
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.utils.getRoleLabelRes
import com.example.urbane.utils.getTipoPropiedadLabelRes


sealed class DialogType {
    object EditSuccess : DialogType()
    object DisableSuccess : DialogType()
    object EnableSuccess: DialogType()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(userId: String, viewmodel: UsersDetailViewModel,usersViewModel: UsersViewModel,residencesViewModel: ResidencesViewModel, sessionManager: SessionManager, goBack:()-> Unit) {
    val state by viewmodel.state.collectAsState()
    val residencesState by residencesViewModel.state.collectAsState()
    var showEnableResidentDialog by remember { mutableStateOf(false) }
    var showEditResidentDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val usuarioDeshabilitado = stringResource(R.string.usuario_deshabilitado_correctamente)
    val usuarioHabilitado = stringResource(R.string.usuario_habilitado)
    val usuarioEditado = stringResource(R.string.usuario_editado_correctamente)

    LaunchedEffect(userId) {
        viewmodel.loadUser(userId)
        residencesViewModel.loadResidences()
    }

    LaunchedEffect(state.success, state.isLoading) {
        if (state.success != null && !state.isLoading) {
            when (state.success) {
                DetailSuccess.UserEnabled -> {
                    snackbarHostState.showSnackbar(
                        message = usuarioHabilitado,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short

                    )
                    viewmodel.resetSuccess()
                }

                DetailSuccess.UserDisabled -> {
                    snackbarHostState.showSnackbar(
                        message = usuarioDeshabilitado,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                    viewmodel.resetSuccess()
                }

                DetailSuccess.UserEdited -> {
                    snackbarHostState.showSnackbar(
                        message = usuarioEditado ,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )

                    viewmodel.resetSuccess()
                }

                null -> TODO()
            }
        }
    }




    if (showEnableResidentDialog) {
        EnableResidentDialog(
            residencesViewModel = residencesViewModel,
            usersDetailViewModel = viewmodel,
            closeDialog = { showEnableResidentDialog = false }
        )
    }

    if (showEditResidentDialog) {
        EditUserDialog(
            user = state.user!!,
            residences = residencesState.residences,
            onDismiss = { showEditResidentDialog = false },
            viewModel = viewmodel,
            onConfirm = { newRoleId, residenceId, _ ->
                viewmodel.processIntent(
                    UsersDetailIntent.EditUser(
                        newRoleId = newRoleId,
                        residenceId = residenceId
                    )
                )
                showEditResidentDialog = false
            }
        )
    }








    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (!state.isLoading && state.user != null) {
                                stringResource(R.string.perfil_de) + " " + state.user!!.name
                            } else {
                                "" // o un placeholder como "Cargando..."
                            },
                            style = MaterialTheme.typography.displayMedium
                        )

                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            usersViewModel.loadUsers()
                            goBack()
                        }) {
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
                sessionManager,
                onShowEnableResidentDialog = { showEnableResidentDialog = true },
                onShowEditResidentDialog = { showEditResidentDialog = true}
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
    sessionManager: SessionManager,
    onShowEnableResidentDialog: () -> Unit,
    onShowEditResidentDialog:() -> Unit
) {
    val state by viewmodel.state.collectAsState()
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val currentUser = userState.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        }


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
                    value = stringResource(getTipoPropiedadLabelRes(user.residence_type))
                )
            }
        }



        Spacer(modifier = Modifier.weight(1f))

        if (currentUser?.userId != user.id) {


            if (user.active == true){
            Button(
                onClick = { onShowEditResidentDialog() },
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
                    onClick = {
                        if (user.role_name == "resident") {
                            onShowEnableResidentDialog()
                           
                        } else {
                            viewmodel.processIntent(UsersDetailIntent.EnableUser)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {

                    if (true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Habilitar", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {

                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


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








