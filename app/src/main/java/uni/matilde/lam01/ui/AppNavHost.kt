package uni.matilde.lam01.ui

import MapViewModelFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uni.matilde.lam01.App
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.audio.AudioViewModelFactory
import uni.matilde.lam01.ui.audio.UserRecordingsViewModel
import uni.matilde.lam01.ui.audio.UserRecordingsViewModelFactory
import uni.matilde.lam01.ui.audio.UserRecordingsScreen
import uni.matilde.lam01.ui.auth.AuthViewModel
import uni.matilde.lam01.ui.auth.AuthViewModelFactory
import uni.matilde.lam01.ui.auth.LoginScreen
import uni.matilde.lam01.ui.auth.SignUpScreen
import uni.matilde.lam01.ui.map.MainMapScreen
import uni.matilde.lam01.ui.map.MapViewModel
import uni.matilde.lam01.util.Exceptions.GlobalExceptionHandler

@Composable
fun AppNavHost(
    isAuthenticated: Boolean,
    preferencesHelper: PreferencesHelper,
    onLoginSuccess: (String) -> Unit,
    onLogout: () -> Unit,
    navigateTo: String?
) {
    val navController = rememberNavController()

    // Configura l'handler globale
    LaunchedEffect(navController) {
        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(navController, onLogout)
        )
    }


    // Naviga alla destinazione specificata se presente
    LaunchedEffect(navigateTo) {
        navigateTo?.let {
            navController.navigate(it) {}
        }
    }

    // Ottiene l'istanza di MapViewModel
    val mapViewModelFactory = MapViewModelFactory(App.instance.mapRepository)
    val mapViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = MapViewModel::class.java,
        factory = mapViewModelFactory
    )

    // Ottiene l'istanza di AuthViewModel
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

    val userRecordingsViewModelFactory = UserRecordingsViewModelFactory(App.instance.audioRepository)
    val userRecordingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = UserRecordingsViewModel::class.java,
        factory = userRecordingsViewModelFactory
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
            MainMapScreen(viewModel = mapViewModel, navController = navController, authViewModel = authViewModel, audioViewModel = audioViewModel, preferencesHelper = preferencesHelper, onLogout = onLogout)
        }

        composable("audios") {
            UserRecordingsScreen(viewModel=userRecordingsViewModel, onBack = { navController.popBackStack() })
        }

        composable("notifications") {
            //todo:schermata delle notifiche
        }
    }
}




