package uni.matilde.lam01.data.remote.repository

import android.util.Log
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.TokenService
import uni.matilde.lam01.data.remote.models.DetailResponse
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.DeleteAccountResponse
import uni.matilde.lam01.data.remote.models.TokenResponse
import uni.matilde.lam01.ui.map.TokenExpiredException

class AuthRepository(
    private val apiService: ApiService,
    private val preferencesHelper: PreferencesHelper,
    private val tokenManager: TokenManager,
    private val tokenService: TokenService
) {

    // Singleton pattern
    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(
            preferencesHelper: PreferencesHelper,
            tokenManager: TokenManager,
            apiService: ApiService,
            tokenService: TokenService
        ): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(
                    apiService = apiService,
                    tokenManager = tokenManager,
                    preferencesHelper = preferencesHelper,
                    tokenService = tokenService
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
                val errorResponse = Gson().fromJson(errorBody, DetailResponse::class.java)
                Result.failure(Exception(errorResponse?.detail ?: "Errore sconosciuto"))
            } else {
                // Altri errori
                Result.failure(Exception("Errore HTTP: ${response.code()}"))
            }
        }
    }

    suspend fun getToken(username: String, password: String): Result<TokenResponse> {
        if (!tokenManager.needsTokenRenewal()) {
            preferencesHelper.saveUsername(username)
            return Result.success(
                TokenResponse(
                    client_secret = tokenManager.getToken()!!,
                    client_id = preferencesHelper.getClientId() ?: -1
                )
            )
        }
        //else:
        return handleApiCall {
            val response = apiService.getToken(username, password)
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    tokenManager.setToken(tokenResponse.client_secret)
                    preferencesHelper.saveToken(tokenResponse.client_secret)
                    preferencesHelper.saveClientId(tokenResponse.client_id)
                    preferencesHelper.saveUsername(username)
                    Result.success(tokenResponse)
                } ?: Result.failure(Exception("Risposta vuota"))
            } else {
                Result.failure(
                    Exception(
                        "Errore durante il login: ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            }
        }
    }


    suspend fun deleteAccount(): Result<DeleteAccountResponse> {
        //TODO: aggiungi rimozione di dati e brani caricati dall'utente
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                Log.d("Delete", "Token utilizzato: $token")
                val response = apiService.deleteAccount(token)

                if (response.isSuccessful) {
                    preferencesHelper.clearPreferences()
                    tokenManager.clearToken()
                    response.body()?.let {
                        Log.d("Delete", "Account eliminato con successo: ${it.toString()}")
                        Result.success(it)
                    } ?: Result.failure(Exception("Risposta vuota"))

                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Errore durante l'eliminazione dell'account: ${response.code()} - ${errorBody ?: "Messaggio sconosciuto"}"
                    Log.e("Delete", errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            }
        }
    }


    /**
     * Funzione generica per gestire le chiamate API
     */
    private suspend fun <T> handleApiCall(apiCall: suspend () -> Result<T>): Result<T> {
        return try {
            apiCall()
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Errore HTTP: ${e.message()}")
            Result.failure(Exception("Errore HTTP: ${e.code()}"))
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> executeAuthenticatedRequest(request: suspend (String) -> Result<T>): Result<T> {
        var token = tokenService.getValidToken()
            ?: return Result.failure(Exception("Token scaduto o non disponibile"))
        token = "Bearer $token"
        return request(token)
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

