package uni.matilde.lam01.data.remote.repository

import retrofit2.Response
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.TokenResponse

class AuthRepository(
    private val apiService: ApiService,
    private val preferencesHelper: PreferencesHelper
) {

    suspend fun signUp(username: String, password: String): Result<AuthResponse> {
        return try {
            val response: Response<AuthResponse> = apiService.signUp(AuthRequest(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Errore durante la registrazione: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getToken(username: String, password: String): Result<TokenResponse> {
        return try {
            val response: Response<TokenResponse> = apiService.getToken(AuthRequest(username, password))
            if (response.isSuccessful) {
                val tokenResponse = response.body()!!
                // Salva il token nelle preferenze
                preferencesHelper.saveToken(tokenResponse.token)
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Credenziali errate: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSongs(): Result<List<AudioEntity>> {
        return try {
            val response: Response<List<AudioEntity>> = apiService.getAllSongs()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Errore nel recupero delle canzoni: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
