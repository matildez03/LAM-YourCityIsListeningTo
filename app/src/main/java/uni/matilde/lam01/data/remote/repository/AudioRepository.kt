package uni.matilde.lam01.data.remote.repository

import android.util.Log
import okhttp3.MultipartBody
import retrofit2.Response
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.local.AudioDao
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.models.AllAudiosResponse
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.data.remote.models.DetailResponse
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import uni.matilde.lam01.data.remote.models.UploadAudioResponse


class AudioRepository(private val apiService: ApiService,
                      private val tokenManager: TokenManager,
                      private val audioDao: AudioDao) {

    // Singleton pattern
    companion object {
        @Volatile
        private var instance: AudioRepository? = null

        fun getInstance(
            apiService: ApiService,
            tokenManager: TokenManager,
            audioDao: AudioDao
        ): AudioRepository {
            return instance ?: synchronized(this) {
                instance ?: AudioRepository(apiService, tokenManager, audioDao).also { instance = it }
            }
        }
    }


    suspend fun uploadAudio(
        longitude: Double,
        latitude: Double,
        file: MultipartBody.Part
    ): Result<UploadAudioResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.uploadAudio(
                    token = token,
                    longitude = longitude,
                    latitude = latitude,
                    file = file
                )
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun saveAudioLocally(audio: AudioEntity) {
        try {
            audioDao.insert(audio)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Errore nel salvataggio locale: ${e.message}")
        }
    }

    suspend fun getAllLocalAudios(): Result<List<AudioEntity>> {
        return try {
            Result.success(audioDao.getAll())
        } catch (e: Exception) {
            Log.e("AudioRepository", "Errore nel recupero dei dati locali: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAllRemoteAudios(): Result<List<AllAudiosResponse>> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getAllSongs(token)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun fetchAudioById(audioInt: Int): Result<AudioResponse>{
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getAudioById(token, audioInt)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun fetchMyAudios(): Result<List<MyAudiosResponse>>{
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getMySongs(token)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun hideSong(audioId: Int): Result<DetailResponse>{
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.hideSong(token,audioId)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun showSong(audioId: Int): Result<MyAudiosResponse>{
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.showSong(token,audioId)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun deleteSong(audioId: Int): Result<DetailResponse>{
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.deleteSong(token,audioId)
                handleRetrofitResponse(response)
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
            Log.e("AudioRepository", "Errore durante la chiamata API: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Esegue una richiesta autenticata recuperando il token dal TokenManager.
     */
    private suspend fun <T> executeAuthenticatedRequest(request: suspend (String) -> Result<T>): Result<T> {
        var token = tokenManager.getToken() ?: return Result.failure(Exception("Token scaduto o non disponibile"))
        token = "Bearer $token"
        return request(token)
    }


    /**
     * Funzione generica per convertire una risposta Retrofit in un oggetto Result
     */
    private inline fun <T> handleRetrofitResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Risposta vuota"))
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception("Errore API: ${response.code()} - $errorBody"))
        }
    }
}