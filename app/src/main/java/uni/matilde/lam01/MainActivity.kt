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
            val isAuthenticated = remember { mutableStateOf(preferencesHelper.getToken() != null) }

            AppNavHost(
                isAuthenticated = isAuthenticated.value,
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

        // Test della connessione diretta
        testConnectionDirect()

    }

    private fun testConnectionDirect() {
        Thread {
            try {
                val url = URL("http://130.136.2.83/lam2024/")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode
                Log.d("ConnectionTest", "Response code: $responseCode")
            } catch (e: Exception) {
                Log.e("ConnectionTest", "Connection error: ${e.message}")
            }
        }.start()
    }
}

