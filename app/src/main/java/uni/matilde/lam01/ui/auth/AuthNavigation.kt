package uni.matilde.lam01.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    navigateToHome: () -> Unit,
    factory: AuthViewModelFactory // Aggiungi il parametro factory
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Schermata di Login
        composable("login") {
            LoginScreen(
                navigateToSignUp = { navController.navigate("signup") },
                navigateToHome = navigateToHome,
                factory = factory // Passa la factory alla LoginScreen
            )
        }

        // Schermata di Registrazione
        composable("signup") {
            SignUpScreen(
                navigateToLogin = { navController.popBackStack("login", inclusive = false) },
                factory = factory // Passa la factory alla SignUpScreen
            )
        }
    }
}

