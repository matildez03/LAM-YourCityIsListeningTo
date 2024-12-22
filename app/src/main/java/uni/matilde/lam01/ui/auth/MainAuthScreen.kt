package uni.matilde.lam01.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainAuthScreen(
    navigateToHome: () -> Unit,
    factory: AuthViewModelFactory // Aggiungi la factory come parametro
) {
    // Controller di navigazione per gestire le schermate
    val navController = rememberNavController()

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
                navigateToLogin = { navController.navigate("login") },
                factory = factory // Passa la factory alla SignUpScreen
            )
        }
    }
}
