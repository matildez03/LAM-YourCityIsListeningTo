package uni.matilde.lam01.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uni.matilde.lam01.App
import uni.matilde.lam01.ui.auth.AuthViewModel
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

    // Ottieni l'istanza di AuthViewModel
    val authViewModelFactory = AuthViewModelFactory(App.instance.authRepository)
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = AuthViewModel::class.java,
        factory = authViewModelFactory
    )


    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "map" else "auth"
    ) {
        composable("auth") {
            MainAuthScreen(
                navigateToHome = {
                    navController.navigate("map") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                factory = AuthViewModelFactory(App.instance.authRepository) // Crea l'istanza della factory
            )
        }
        composable("map") { //la schermata principale è direttamente la mappa
            MainMapScreen(navController = navController, authViewModel = authViewModel )
        }

        composable("audios") {
            //todo("crea composable della schermata della gestione degli audio)
        }

        composable("notifications") {
            //todo:schermata delle notifiche
        }
    }
}




