package uni.matilde.lam01.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uni.matilde.lam01.App
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.audio.AudioViewModelFactory
import uni.matilde.lam01.ui.auth.AuthViewModel
import uni.matilde.lam01.ui.auth.AuthViewModelFactory
import uni.matilde.lam01.ui.auth.LoginScreen
import uni.matilde.lam01.ui.auth.SignUpScreen
import uni.matilde.lam01.ui.map.MainMapScreen

@Composable
fun AppNavHost(
    isAuthenticated: Boolean,
    preferencesHelper: PreferencesHelper,
    onLoginSuccess: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // Ottieni l'istanza di AuthViewModel
    val authViewModelFactory =
        AuthViewModelFactory(App.instance.authRepository)
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = AuthViewModel::class.java,
        factory = authViewModelFactory
    )

    // Ottiene l'istanza di AudioViewModel
    val audioViewModelFactory = AudioViewModelFactory(App.instance.audioRepository)
    val audioViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = AudioViewModel::class.java,
        factory = audioViewModelFactory
    )


    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "map" else "login"
    ) {
        composable("login") {
            LoginScreen(
                navigateToSignUp = { navController.navigate("signup") },
                navigateToHome = {
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                factory = authViewModelFactory // Passa la factory alla LoginScreen
            )
        }

        composable("signup"){
            SignUpScreen(
                navigateToLogin = { navController.navigate("login") },
                factory = authViewModelFactory // Passa la factory alla SignUpScreen
            )
        }
        composable("map") { //la schermata principale è direttamente la mappa
            MainMapScreen(navController = navController, authViewModel = authViewModel, audioViewModel = audioViewModel, preferencesHelper = preferencesHelper)
        }

        composable("audios") {
            //todo("crea composable della schermata della gestione degli audio)
        }

        composable("notifications") {
            //todo:schermata delle notifiche
        }
    }
}




