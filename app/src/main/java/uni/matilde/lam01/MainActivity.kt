package uni.matilde.lam01

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import uni.matilde.lam01.ui.AppNavHost
import java.net.URL
import java.net.HttpURLConnection


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")

        setContent {
            val preferencesHelper = App.instance.preferencesHelper

            // Controlla se l'utente è autenticato
            val isAuthenticated =
                remember { mutableStateOf(preferencesHelper.getToken() != null && preferencesHelper.getUsername() != null) }

            //debug
            Log.d("MainActivity", "Token: ${preferencesHelper.getToken()}")
            Log.d("MainActivity", "Username: ${preferencesHelper.getUsername()}")

            // Legge l'intent per verificare se esiste un'istruzione di navigazione
            val navigateTo = intent?.getStringExtra("navigate_to")



            AppNavHost(
                isAuthenticated = isAuthenticated.value,
                preferencesHelper = preferencesHelper,
                navigateTo = navigateTo // Passa l'istruzione di navigazione
            )
        }
        Log.d("MainActivity", "onCreate ended")

    }
}

