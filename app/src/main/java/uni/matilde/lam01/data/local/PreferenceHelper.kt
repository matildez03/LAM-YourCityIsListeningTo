package uni.matilde.lam01.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData

class PreferencesHelper(context: Context) {

    companion object {
        private const val PREFS_NAME = "auth_prefs" // Nome del file delle SharedPreferences
        private const val KEY_AUTH_TOKEN = "auth_token" // Chiave per il token
        private const val KEY_USERNAME = "username" // Chiave per il nome utente
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Salva il token di autenticazione
    fun saveToken(token: String) {
        Log.d("PreferencesHelper", "Saving token: $token")
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    // Recupera il token di autenticazione
    fun getToken(): String? {
        val token = sharedPreferences.getString("token", null)
        Log.d("PreferencesHelper", "Token letto: $token") //debug
        return token
    }

    // LiveData per osservare il token
    fun observeToken(): LiveData<String?> {
        return SharedPreferencesLiveData(sharedPreferences, KEY_AUTH_TOKEN)
    }

    // Salva il nome utente (esempio)
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    // Recupera il nome utente (esempio)
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    // Cancella tutte le preferenze salvate (ad esempio, durante il logout)
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    fun clearToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }

    // Implementazione delle sharedPreferences sottoforma di livedata
    class SharedPreferencesLiveData(
        private val sharedPreferences: SharedPreferences,
        private val key: String
    ) : LiveData<String?>() {

        private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, updatedKey ->
            if (updatedKey == key) {
                value = sharedPreferences.getString(key, null)
            }
        }

        override fun onActive() {
            super.onActive()
            value = sharedPreferences.getString(key, null)
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun onInactive() {
            super.onInactive()
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
