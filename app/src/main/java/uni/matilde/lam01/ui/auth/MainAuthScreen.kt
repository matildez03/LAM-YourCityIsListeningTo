package uni.matilde.lam01.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainAuthScreen(
    navigateToHome: () -> Unit,
    factory: AuthViewModelFactory
) {
    // Controller di navigazione per gestire le schermate
    val navController = rememberNavController()

    // Crea il ViewModel per la gestione dello stato di autenticazione
    val authViewModel = factory.create(AuthViewModel::class.java)
    val authState by authViewModel.authState.observeAsState()

    // Osserva lo stato di autenticazione e reindirizza al login se necessario
    when (authState) {
        is AuthState.Error -> {
            val message = (authState as AuthState.Error).message
            if (message == "Sessione scaduta. Effettua il login.") {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true } // Torna alla schermata login
                }
                authViewModel.resetState() // Resetta lo stato per evitare loop
            }
        }
        else -> { /* Altri stati, come caricamento o inattivo */ }
    }

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
