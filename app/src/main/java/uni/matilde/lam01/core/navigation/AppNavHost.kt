package uni.matilde.lam01.core.navigation

import uni.matilde.lam01.features.map.viewmodel.MapViewModelFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uni.matilde.lam01.core.app.App
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.features.audio.viewmodel.AudioViewModel
import uni.matilde.lam01.features.audio.viewmodel.AudioViewModelFactory
import uni.matilde.lam01.features.audio.viewmodel.UserRecordingsViewModel
import uni.matilde.lam01.features.audio.viewmodel.UserRecordingsViewModelFactory
import uni.matilde.lam01.features.audio.ui.UserRecordingsScreen
import uni.matilde.lam01.features.auth.viewmodel.AuthViewModel
import uni.matilde.lam01.features.auth.viewmodel.AuthViewModelFactory
import uni.matilde.lam01.features.auth.ui.LoginScreen
import uni.matilde.lam01.features.auth.ui.SignUpScreen
import uni.matilde.lam01.features.map.ui.MainMapScreen
import uni.matilde.lam01.features.map.viewmodel.MapViewModel
import uni.matilde.lam01.features.stats.ui.StatisticsScreen
import uni.matilde.lam01.features.stats.viewmodel.StatisticsViewModel
import uni.matilde.lam01.features.stats.viewmodel.StatisticsViewModelFactory
import uni.matilde.lam01.core.util.Exceptions.GlobalExceptionHandler

@Composable
fun AppNavHost(
    isAuthenticated: Boolean,
    preferencesHelper: PreferencesHelper,
    navigateTo: String?
) {
    val navController = rememberNavController()

    // Configura l'handler globale
    LaunchedEffect(navController) {
        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(navController)
        )
    }


    // Naviga alla destinazione specificata se presente
    LaunchedEffect(navigateTo) {
        navigateTo?.let {
            navController.navigate(it) {}
        }
    }

    val mapViewModelFactory = MapViewModelFactory(App.instance.mapRepository)
    val mapViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = MapViewModel::class.java,
        factory = mapViewModelFactory
    )

    val authViewModelFactory =
        AuthViewModelFactory(App.instance.authRepository)
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        modelClass = AuthViewModel::class.java,
        factory = authViewModelFactory
    )

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

    val statisticsViewModelFactory = StatisticsViewModelFactory(App.instance.audioRepository)
    val statisticsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
    modelClass = StatisticsViewModel::class.java,
    factory = statisticsViewModelFactory
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
                factory = authViewModelFactory
            )
        }

        composable("signup"){
            SignUpScreen(
                navigateToLogin = { navController.navigate("login") },
                factory = authViewModelFactory
            )
        }

        composable("map") {
            MainMapScreen(viewModel = mapViewModel, navController = navController, authViewModel = authViewModel, audioViewModel = audioViewModel, preferencesHelper = preferencesHelper)
        }

        composable("menu") {
            MenuContent(navController=navController,authViewModel=authViewModel,preferencesHelper=preferencesHelper)
        }

        composable("audios") {
            UserRecordingsScreen(viewModel=userRecordingsViewModel, navController=navController)
        }

        composable("stats") {
            StatisticsScreen(statisticsViewModel, navController)
        }
    }
}




