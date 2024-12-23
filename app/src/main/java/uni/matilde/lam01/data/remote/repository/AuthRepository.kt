package uni.matilde.lam01.data.remote.repository

import android.util.Log
import com.google.gson.Gson
import retrofit2.Response
import uni.matilde.lam01.App
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.api.RetrofitInstance
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.TokenManager
import uni.matilde.lam01.data.remote.models.AuthErrorResponse
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.DeleteAccountResponse
import uni.matilde.lam01.data.remote.models.TokenResponse
import uni.matilde.lam01.ui.map.TokenExpiredException

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

    suspend fun <T> executeAuthenticatedRequest(
        request: suspend (String) -> T
    ): T {
        val token = TokenManager.getToken() ?: throw TokenExpiredException()
        return request(token)
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
        // Controlla se esiste un token valido
        val existingToken = TokenManager.getToken()
        if (existingToken != null) {
            val clientId = preferencesHelper.getClientId() // Recupera l'username
            if (clientId == null) {
                // L'username non è presente: errore
                return Result.failure(Exception("client_id mancante: impossibile creare TokenResponse"))
            }

            return Result.success(
                TokenResponse(client_secret = existingToken, client_id = clientId)
            )
        }

        // Effettua la richiesta al server solo se necessario
        return handleApiCall {
            val response = apiService.getToken(username, password)
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    TokenManager.setToken(tokenResponse.client_secret)
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Risposta vuota dal server"))
            } else {
                Result.failure(Exception("Errore: ${response.errorBody()?.string()}"))
            }
        }
    }

    suspend fun deleteAccount(): Result<DeleteAccountResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.deleteAccount(token)
                response.toResult()
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

