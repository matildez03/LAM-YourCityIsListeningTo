package uni.matilde.lam01.data.remote

import kotlinx.coroutines.*
import uni.matilde.lam01.data.local.PreferencesHelper


object TokenManager {

    private var token: String? = null
    private var expirationTime: Long? = null
    private var timerJob: Job? = null

    private lateinit var preferencesHelper: PreferencesHelper

    // Inizializza il TokenManager
    fun initialize(preferencesHelper: PreferencesHelper) {
        this.preferencesHelper = preferencesHelper
        token = preferencesHelper.getToken()
        expirationTime = preferencesHelper.getTokenExpirationTime()
        if (isTokenExpired()) {
            clearToken()
        } else {
            startExpirationTimer()
        }
    }

    // Imposta un nuovo token e avvia il timer di scadenza
    fun setToken(newToken: String) {
        token = newToken
        expirationTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 ora
        preferencesHelper.saveToken(newToken)
        preferencesHelper.saveTokenExpirationTime(expirationTime!!)
        startExpirationTimer()
    }

    // Ottieni il token attuale (null se scaduto)
    fun getToken(): String? {
        if (isTokenExpired()) {
            clearToken()
        }
        return token
    }

    // Verifica se il token è scaduto
    private fun isTokenExpired(): Boolean {
        return expirationTime?.let { System.currentTimeMillis() > it } ?: true
    }

    // Cancella il token
    private fun clearToken() {
        token = null
        expirationTime = null
        timerJob?.cancel()
        preferencesHelper.clearToken()
    }

    // Avvia un timer per invalidare il token
    private fun startExpirationTimer() {
        timerJob?.cancel() // Cancella eventuali timer precedenti
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            delay(60 * 60 * 1000) // 1 ora
            clearToken()
        }
    }
}
