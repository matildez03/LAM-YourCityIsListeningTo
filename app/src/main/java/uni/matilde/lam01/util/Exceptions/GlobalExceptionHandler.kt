package uni.matilde.lam01.util.Exceptions

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uni.matilde.lam01.App

class GlobalExceptionHandler(
    private val navController: NavController
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (e is AuthenticationException) {

            //Usa Dispatchers.Main perché sta lavorando sulla navigazione UI
            CoroutineScope(Dispatchers.Main).launch {
                App.instance.userSessionManager.onLogout()

                navController.navigate(route = "login") {
                    popUpTo(route = "login") { inclusive = true } // Cancella lo stack di navigazione
                }
            }
        } else {
            // Lascia che il gestore predefinito gestisca altre eccezioni
            defaultHandler?.uncaughtException(t, e)
        }
    }
}
