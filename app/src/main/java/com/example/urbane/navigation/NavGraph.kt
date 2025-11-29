package com.example.urbane.navigation

import AddResidenceScreen
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
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
import com.example.urbane.ui.admin.claims.view.ClaimsScreen
import com.example.urbane.ui.admin.residences.model.ResidencesDetailSuccess
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

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("ViewModelConstructorInComposable", "ComposableDestinationInComposeScope")
@Composable
fun MainNavigation(navController: NavHostController, modifier: Modifier) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val loginViewModel = LoginViewModel(sessionManager)
    val residencesViewModel = ResidencesViewModel(sessionManager)
    val usersViewModel = UsersViewModel(sessionManager)
    val usersDetailViewModel = UsersDetailViewModel(sessionManager)
    val residencesDetailViewModel = ResidencesDetailViewModel(sessionManager)


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
                usersViewModel

            )
        }
        composable(Routes.ADMIN_RESIDENCES) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_RESIDENCES,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel


            )

        }
        composable(Routes.ADMIN) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel

            )

        }
        composable(Routes.ADMIN_PAYMENTS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_PAYMENTS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel

            )
        }
        composable(Routes.ADMIN_CLAIMS) {
            AdminMainScaffold(
                navController = navController,
                currentRoute = Routes.ADMIN_CLAIMS,
                loginViewModel,
                sessionManager,
                residencesViewModel,
                usersViewModel

            )
        }
        composable(Routes.ADMIN_USERS_ADD) {
            AddUserScreen(usersViewModel, residencesViewModel){
                navController.popBackStack()
            }
        }
        composable(Routes.ADMIN_RESIDENCES_ADD) {
            val residencesViewModel = ResidencesViewModel(sessionManager)
            AddResidenceScreen(residencesViewModel){
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
            Routes.ADMIN_RESIDENCES_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            ResidencesDetailScreen(
                residenceId = backStackEntry.arguments?.getInt("id") ?: 0,
                viewmodel = residencesDetailViewModel,
            ){
                navController.popBackStack()
            }
        }


    }
}














