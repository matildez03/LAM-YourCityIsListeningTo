package uni.matilde.lam01.data.remote.repository

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import retrofit2.Response
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.TokenService
import uni.matilde.lam01.data.local.AudioDao
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AllAudiosResponse
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.data.remote.models.DetailResponse
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import uni.matilde.lam01.data.remote.models.UploadAudioResponse
import uni.matilde.lam01.ui.map.TokenExpiredException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
Accesso a dati remoti e locali
Naming convention:
- get per dati locali
- fetch per dati remoti
 */
class AudioRepository(
    private val apiService: ApiService,
    private val tokenService: TokenService,
    private val preferencesHelper: PreferencesHelper,
    private val audioDao: AudioDao
) {

    // Singleton pattern
    companion object {
        @Volatile
        private var instance: AudioRepository? = null

        fun getInstance(
            apiService: ApiService,
            tokenService: TokenService,
            preferencesHelper: PreferencesHelper,
            audioDao: AudioDao
        ): AudioRepository {
            return instance ?: synchronized(this) {
                instance ?: AudioRepository(
                    apiService,
                    tokenService,
                    preferencesHelper,
                    audioDao
                ).also {
                    instance = it
                }
            }
        }
    }


    // Dati remoti

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

    suspend fun uploadAndSaveAudio(
        username: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Result<AudioEntity> {
        return handleApiCall {
            try {
                val file = File(filePath)
                val requestFile = file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val audioResponse = uploadAudio(longitude, latitude, body).getOrThrow()
                // Ottiene l'id del brano nel server
                val serverAudioId = fetchMyAudios()
                    .getOrNull()
                    ?.lastOrNull()
                    ?.id
                    ?: throw IllegalStateException("Impossibile ottenere l'ID audio dal server.")

                // Ottine il timestamp
                val currentDateTime = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedTimestamp = formatter.format(currentDateTime).toString()

                // Salva in locale
                val audioEntity = AudioEntity(
                    id = serverAudioId!!,
                    username = username,
                    locationName = locationName,
                    filePath = filePath,
                    bpm = audioResponse.bpm,
                    danceability = audioResponse.danceability,
                    loudness = audioResponse.loudness,
                    mood = audioResponse.mood.maxByOrNull { it.value }?.key,
                    genre = audioResponse.genre.maxByOrNull { it.value }?.key,
                    instrument = audioResponse.instrument.maxByOrNull { it.value }?.key,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = formattedTimestamp
                )
                saveAudioLocally(audioEntity)

                Log.i("AudioRepository", "Audio caricato con successo")
                Result.success(audioEntity)
            } catch (e: Exception) {
                Log.e("AudioRepository", "Errore durante l'upload con salvataggio: ${e.message}")
                Result.failure(e)
            }
        }
    }


    suspend fun fetchAllRemoteAudios(): Result<List<AllAudiosResponse>> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getAllSongs(token)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun fetchAudioById(audioInt: Int): Result<AudioResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getAudioById(token, audioInt)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun fetchMyAudios(): Result<List<MyAudiosResponse>> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.getMySongs(token)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun hideSong(audioId: Int): Result<DetailResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.hideSong(token, audioId)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun showSong(audioId: Int): Result<MyAudiosResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.showSong(token, audioId)
                handleRetrofitResponse(response)
            }
        }
    }

    suspend fun deleteSong(audioId: Int): Result<DetailResponse> {
        return handleApiCall {
            executeAuthenticatedRequest { token ->
                val response = apiService.deleteSong(token, audioId)
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
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Errore HTTP: ${e.message()}")
            Result.failure(Exception("Errore HTTP: ${e.code()}"))
        }catch (e: Exception) {
            Log.e("AudioRepository", "Errore durante la chiamata API: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Esegue una richiesta autenticata recuperando il token dal TokenManager.
     */
    private suspend fun <T> executeAuthenticatedRequest(request: suspend (String) -> Result<T>): Result<T> {
        var token = tokenService.getValidToken()
            ?: return Result.failure(Exception("Token scaduto o non disponibile"))
        token = "Bearer $token"
        return request(token)
    }


    /**
     * Funzione generica per convertire una risposta Retrofit in un oggetto Result
     */
    private inline fun <T> handleRetrofitResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Risposta vuota"))
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception("Errore API: ${response.code()} - $errorBody"))
        }
    }

    /***************************************************************************************/
    // DATI LOCALI

    suspend fun saveAudioLocally(audio: AudioEntity) {
        try {
            audioDao.insert(audio)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Errore nel salvataggio locale: ${e.message}")
        }
    }

    suspend fun getAllMyLocalAudios(): Result<List<AudioEntity>> {
        return try {
            val username = preferencesHelper.getUsername()
            if (username != null) {
                Result.success(audioDao.getAllByUsername(username))
            } else {
                Result.failure(Exception("Username non trovato. Probabilmente il token è scaduto."))
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Errore nel recupero dei dati locali: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLocalAudioById(audioId: Int): Result<AudioEntity> {
        return try {
            Result.success(audioDao.getById(audioId))
        } catch (e: Exception) {
            Log.e("AudioRepository", "Errore nel recupero dei dati locali: ${e.message}")
            Result.failure(e)
        }
    }
}