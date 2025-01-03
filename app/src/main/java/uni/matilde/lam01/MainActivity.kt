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



            AppNavHost(
                isAuthenticated = isAuthenticated.value,
                preferencesHelper = preferencesHelper,
                onLoginSuccess = { token ->
                    // Salva il token e aggiorna lo stato
                    preferencesHelper.saveToken(token)
                    isAuthenticated.value = true
                },
                onLogout = {
                    // Rimuove il token e aggiorna lo stato
                    preferencesHelper.clearToken()
                    isAuthenticated.value = false
                }
            )
        }
        Log.d("MainActivity", "onCreate ended")

    }
}

