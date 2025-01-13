package uni.matilde.lam01.util.Exceptions

import androidx.navigation.NavController

class GlobalExceptionHandler(
    private val navController: NavController,
    private val onLogout: () -> Unit
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (e is AuthenticationException) {
            // Esegui il logout
            onLogout()
            // Reindirizza al login
            navController.navigate("login") {
                popUpTo("login") { inclusive = true } // Cancella lo stack di navigazione
            }
        } else {
            // Lascia che il gestore predefinito gestisca altre eccezioni
            defaultHandler?.uncaughtException(t, e)
        }
    }
}
