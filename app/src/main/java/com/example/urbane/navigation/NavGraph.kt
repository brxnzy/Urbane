package com.example.urbane.navigation

import AddResidenceScreen
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.urbane.data.local.SessionManager
import com.example.urbane.ui.Splash
import com.example.urbane.ui.admin.AdminMainScaffold
import com.example.urbane.ui.admin.contracts.view.ContractDetailScreen
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsDetailViewModel
import com.example.urbane.ui.admin.contracts.viewmodel.ContractsViewModel
import com.example.urbane.ui.admin.fines.view.AddFineScreen
import com.example.urbane.ui.admin.fines.view.FinesDetailScreen
import com.example.urbane.ui.admin.fines.viewmodel.FinesDetailViewModel
import com.example.urbane.ui.admin.fines.viewmodel.FinesViewModel
import com.example.urbane.ui.admin.payments.viewmodel.PaymentsViewModel
import com.example.urbane.ui.admin.residences.view.ResidencesDetailScreen
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesDetailViewModel
import com.example.urbane.ui.admin.residences.viewmodel.ResidencesViewModel
import com.example.urbane.ui.admin.users.view.AddUserScreen
import com.example.urbane.ui.admin.users.view.UserDetailScreen
import com.example.urbane.ui.admin.users.viewmodel.UsersDetailViewModel
import com.example.urbane.ui.admin.users.viewmodel.UsersViewModel
import com.example.urbane.ui.auth.view.DisabledScreen
import com.example.urbane.ui.auth.view.LoginScreen
import com.example.urbane.ui.auth.view.RegisterScreen
import com.example.urbane.ui.auth.viewmodel.LoginViewModel
import com.example.urbane.ui.auth.viewmodel.RegisterViewModel
import com.example.urbane.ui.resident.view.ResidentScreen
import com.example.urbane.ui.resident.viewmodel.PagosViewModel

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("ViewModelConstructorInComposable", "ComposableDestinationInComposeScope")
@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val loginViewModel = LoginViewModel(sessionManager)
    val residencesViewModel = ResidencesViewModel(sessionManager)
    val usersViewModel = UsersViewModel(sessionManager)
    val pagosViewModel = PagosViewModel()
    val usersDetailViewModel = UsersDetailViewModel(sessionManager)
    val residencesDetailViewModel = ResidencesDetailViewModel(sessionManager)
    val contractsViewModel = ContractsViewModel(sessionManager)
    val contractsDetailViewModel = ContractsDetailViewModel(sessionManager)
    val paymentsViewModel = PaymentsViewModel(sessionManager)
    val finesViewModel = FinesViewModel(sessionManager)
    val finesDetailViewModel = FinesDetailViewModel(sessionManager)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            Splash(sessionManager = sessionManager) { role ->
                when (role) {
                    "1" -> navController.navigate(Routes.ADMIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }

                    "2" -> navController.navigate(Routes.RESIDENT) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }

                    else -> navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
        }
        composable(Routes.REGISTER) {
            val registerViewModel = RegisterViewModel()
            RegisterScreen(
                registerViewModel,
                navController,
                modifier = modifier,
                toLogin = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                loginViewModel,
                sessionManager,
                navController,
                modifier = modifier,
                toRegister = { navController.navigate(Routes.REGISTER) },
                navigateByRole = { roleId ->
                    when (roleId) {
                        "1" -> navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }

                        "2" -> navController.navigate(Routes.RESIDENT) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }

                        else -> navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Routes.ADMIN_USERS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_USERS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel
                )
        }
        composable(Routes.ADMIN_RESIDENCES) {
            var residenceDeleted by remember { mutableStateOf(false) }
            DisposableEffect(Unit) {
                val deleted = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<Boolean>("residenceDeleted") ?: false

                if (deleted) {
                    residenceDeleted = true
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("residenceDeleted")
                }
                onDispose { }
            }
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_RESIDENCES,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel,
                showResidenceDeletedMessage = residenceDeleted
            )
        }

        composable(Routes.ADMIN) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel
                )
        }

        composable(Routes.ADMIN_PAYMENTS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_PAYMENTS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel

                )
        }

        composable(Routes.ADMIN_FINES) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_FINES,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel

            )
        }
        composable(Routes.ADMIN_CLAIMS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_CLAIMS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel



                )
        }

        composable(Routes.ADMIN_CONTRACTS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_CONTRACTS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel,
                contractsViewModel,
                paymentsViewModel,
                finesViewModel
                )
        }
        composable(Routes.ADMIN_USERS_ADD) {
            AddUserScreen(usersViewModel, residencesViewModel){
                navController.popBackStack()
            }
        }

        composable(Routes.ADMIN_RESIDENCES_ADD) {
            AddResidenceScreen(residencesViewModel){
                navController.popBackStack()
            }
        }

        composable(Routes.ADMIN_FINES_ADD) {
            AddFineScreen(finesViewModel){
                navController.popBackStack()
            }
        }
        composable(Routes.RESIDENT){
            ResidentScreen(sessionManager, loginViewModel, navController)
        }
        composable(Routes.DISABLED){
            DisabledScreen {
                navController.navigate(Routes.LOGIN)
            }
        }
        composable(
            Routes.ADMIN_USERS_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            UserDetailScreen(
                userId = backStackEntry.arguments?.getString("id") ?: "",
                viewmodel = usersDetailViewModel,
                usersViewModel,
                residencesViewModel,
                sessionManager
            ){
                navController.popBackStack()
            }
        }

        composable(
            Routes.ADMIN_CONTRACTS_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            ContractDetailScreen(
                contractId = backStackEntry.arguments?.getString("id") ?: "",
                viewmodel = contractsDetailViewModel,
            ){
                navController.popBackStack()
            }
        }
        composable(
            Routes.ADMIN_RESIDENCES_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            ResidencesDetailScreen(
                residenceId = backStackEntry.arguments?.getInt("id") ?: 0,
                viewmodel = residencesDetailViewModel,
            ){showDeleteMessage ->
                if (showDeleteMessage) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("residenceDeleted", true)
                }
                navController.popBackStack()
            }

        }


        composable(
            Routes.ADMIN_FINES_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
           FinesDetailScreen(
                fineId = backStackEntry.arguments?.getString("id") ?: "",
                viewModel = finesDetailViewModel,
            ){
                navController.popBackStack()
            }
        }


    }
}