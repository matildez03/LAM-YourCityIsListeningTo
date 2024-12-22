package uni.matilde.lam01.data.remote.repository

import android.util.Log
import com.google.gson.Gson
import retrofit2.Response
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.api.RetrofitInstance
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AuthErrorResponse
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.TokenResponse

class AuthRepository(
    private val apiService: ApiService,
    private val preferencesHelper: PreferencesHelper
) {

    // Singleton pattern
    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(preferencesHelper: PreferencesHelper): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(
                    apiService = RetrofitInstance.api, // Usa RetrofitInstance.api
                    preferencesHelper = preferencesHelper
                ).also { instance = it }
            }
        }
    }

    suspend fun signUp(username: String, password: String): Result<AuthResponse> {
        return handleApiCall {
            val authRequest = AuthRequest(username, password)
            Log.d("Retrofit", "Requesting signup with body: ${Gson().toJson(authRequest)}")
            val response: Response<AuthResponse> = apiService.signUp(authRequest)

            if (response.isSuccessful) {
                // Successo: ritorna il risultato
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Risposta vuota"))
            } else if (response.code() == 400) {
                // Caso specifico: Username già registrato
                val errorBody = response.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, AuthErrorResponse::class.java)
                Result.failure(Exception(errorResponse?.detail ?: "Errore sconosciuto"))
            } else {
                // Altri errori
                Result.failure(Exception("Errore HTTP: ${response.code()}"))
            }
        }
    }

    suspend fun getToken(username: String, password: String): Result<TokenResponse> {
        return handleApiCall {
            val response: Response<TokenResponse> = apiService.getToken(AuthRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    // Salva il token nelle preferenze
                    preferencesHelper.saveToken(tokenResponse.token)
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Risposta vuota dal server"))
            } else {
                Result.failure(Exception("Errore durante l'autenticazione: ${response.errorBody()?.string()}"))
            }
        }
    }

    /**
     * Funzione generica per gestire le chiamate API
     */
    private suspend fun <T> handleApiCall(apiCall: suspend () -> Result<T>): Result<T> {
        return try {
            apiCall()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Estensione per convertire una risposta Retrofit in un oggetto Result
     */
    private fun <T> Response<T>.toResult(): Result<T> {
        return if (this.isSuccessful) {
            this.body()?.let { Result.success(it) } ?: Result.failure(Exception("Risposta vuota"))
        } else {
            Result.failure(Exception("Errore API: ${this.errorBody()?.string()}"))
        }
    }
}

