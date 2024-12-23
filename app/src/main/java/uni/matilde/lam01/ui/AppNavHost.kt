package uni.matilde.lam01.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uni.matilde.lam01.App
import uni.matilde.lam01.ui.auth.AuthViewModelFactory
import uni.matilde.lam01.ui.auth.MainAuthScreen
import uni.matilde.lam01.ui.map.MainMapScreen

@Composable
fun AppNavHost(
    isAuthenticated: Boolean,
    onLoginSuccess: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // repository dal contesto globale
    val authRepository = App.instance.authRepository

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "map" else "auth"
    ) {
        composable("auth") {
            MainAuthScreen(
                navigateToHome = {
                    onLoginSuccess("exampleToken")
                    navController.navigate("map") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                factory = AuthViewModelFactory(App.instance.authRepository) // Crea l'istanza della factory
            )
        }
        composable("map") { //la schermata principale è direttamente la mappa
            MainMapScreen(navController = navController)
        }
    }
}




