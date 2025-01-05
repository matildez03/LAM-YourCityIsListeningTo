package uni.matilde.lam01.data.remote.repository

import com.google.android.gms.maps.model.LatLng
import uni.matilde.lam01.api.ApiService
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.ui.map.MapMarker

/*
Classe creata per rispettare il SRP
 */
class MapRepository(private val audioRepository: AudioRepository) {

    // Singleton pattern
    companion object {
        @Volatile
        private var instance: MapRepository? = null

        fun getInstance(
            audioRepository: AudioRepository
        ): MapRepository {
            return instance ?: synchronized(this) {
                instance ?: MapRepository(
                    audioRepository = audioRepository
                ).also { instance = it }
            }
        }
    }

    // Ottieni tutti gli audio come marker per la mappa
    suspend fun getMarkers(): Result<List<MapMarker>> {
        return try {
            val result = audioRepository.getAllRemoteAudios()
            if (result.isSuccess) {
                val markers = result.getOrNull()?.map { audio ->
                    MapMarker(
                        position = LatLng(audio.latitude, audio.longitude),
                        audioId = audio.id
                    )
                } ?: emptyList()
                Result.success(markers)
            } else {
                Result.failure(Exception("Failed to fetch markers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ottieni informazioni specifiche di un audio
    suspend fun getAudioInfo(audioId: Int): Result<AudioResponse> {
        return audioRepository.fetchAudioById(audioId)
    }
}
