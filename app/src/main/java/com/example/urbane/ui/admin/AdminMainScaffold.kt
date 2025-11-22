package com.example.urbane.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.urbane.R
import com.example.urbane.navigation.Routes
import com.example.urbane.ui.admin.users.view.UsersScreen
import com.example.urbane.ui.admin.residences.view.ResidencesScreen
import com.example.urbane.ui.admin.payments.PaymentsScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.admin.claims.view.ClaimsScreen
import com.example.urbane.ui.admin.contracts.view.ContractsScreen
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.view.TestScreen
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.ui.auth.viewmodel.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScaffold(
    navController: NavHostController,
    currentRoute: String,
    loginViewModel: LoginViewModel,
    sessionManager: SessionManager,
    residencesViewModel: ResidencesViewModel,
    usersViewModel: UsersViewModel


    ) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(290.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                DrawerContent(sessionManager,navController,loginViewModel,currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.ADMIN) { inclusive = false }
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text= when(currentRoute){
                            Routes.ADMIN_USERS -> stringResource(R.string.usuarios)
                            Routes.ADMIN_PAYMENTS -> stringResource(R.string.pagos)
                            Routes.ADMIN_RESIDENCES -> stringResource(R.string.residencias)
                            Routes.ADMIN_CLAIMS -> stringResource(R.string.reclamos)
                            Routes.ADMIN_CONTRACTS -> stringResource(R.string.contratos)
                            Routes.ADMIN -> "Dashboard"
                            else -> "Panel Admin"
                        }, style = MaterialTheme.typography.displayMedium)


                            },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Panel", modifier = Modifier.size(30.dp))
                        }
                    },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )

                )

            }
        )
        { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentRoute) {
                    Routes.ADMIN_USERS -> UsersScreen(
                        usersViewModel,
                        modifier = Modifier.padding(16.dp),
                        navController = navController
                    )
                    Routes.ADMIN_RESIDENCES -> ResidencesScreen(residencesViewModel,navController,modifier = Modifier.padding(16.dp))
                    Routes.ADMIN_CLAIMS-> ClaimsScreen()
                    Routes.ADMIN_PAYMENTS -> TestScreen()
                    Routes.ADMIN_CONTRACTS -> ContractsScreen()
                    Routes.ADMIN -> Dashboard(sessionManager)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(sessionManager: SessionManager,navController: NavHostController,loginViewModel: LoginViewModel, currentRoute: String, onDestinationClicked: (String) -> Unit) {
    val userState = sessionManager.sessionFlow.collectAsState(initial = null)
    val user = userState.value

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween

    ) {


    Column(modifier = Modifier
        .padding(top = 35.dp,
            )
    ) {

        if (user?.userData?.residential?.logoUrl?.isBlank() == true) {
            Image(
                painter = rememberAsyncImagePainter(user.userData.residential.logoUrl),
                contentDescription = "Logo del residencial",
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(100.dp)
            )
        } else {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .padding(start = 15.dp)
        )

    }


        Text(
            user?.userData?.residential?.name ?: "Panel" ,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))
        DrawerItem("Dashboard", Icons.Outlined.Dashboard , Routes.ADMIN, currentRoute, onDestinationClicked)
        DrawerItem(stringResource(R.string.usuarios), Icons.Outlined.Person, Routes.ADMIN_USERS, currentRoute, onDestinationClicked)
        DrawerItem(stringResource(R.string.residencias), Icons.Outlined.House, Routes.ADMIN_RESIDENCES, currentRoute, onDestinationClicked)
        DrawerItem("Reclamos",Icons.Outlined.ReportProblem, Routes.ADMIN_CLAIMS, currentRoute, onDestinationClicked)
        DrawerItem("Pagos", Icons.Outlined.Payments, Routes.ADMIN_PAYMENTS, currentRoute, onDestinationClicked)
        DrawerItem(stringResource(R.string.contratos), Icons.Outlined.Assignment, Routes.ADMIN_CONTRACTS, currentRoute, onDestinationClicked)

    }

    Column(modifier = Modifier.padding(bottom = 45.dp)) {

        Button(
            onClick = {loginViewModel.onLogoutClicked { navController.navigate(Routes.LOGIN)
            {popUpTo(0) { inclusive = true }
                launchSingleTop = true } }},

            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White

            ),
            shape =  RoundedCornerShape(
                topStart = 10.dp,
                bottomStart = 10.dp,
                topEnd = 10.dp,
                bottomEnd = 10.dp
            )
        ) {

            Row (modifier = Modifier.padding(vertical = 5.dp),horizontalArrangement = Arrangement.spacedBy(10.dp)

            ){

                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesion")
                Text("Cerrar Sesion", style = MaterialTheme.typography.bodyMedium)

            }
        }

    }

    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    route: String,
    currentRoute: String,
    onClick: (String) -> Unit
) {
    val isSelected = route == currentRoute

    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else Color.Transparent

    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding(top = 4.dp, bottom = 4.dp, end = 10.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 35.dp,
            bottomEnd = 35.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(color = contentColor)
            )
        }
    }
}
