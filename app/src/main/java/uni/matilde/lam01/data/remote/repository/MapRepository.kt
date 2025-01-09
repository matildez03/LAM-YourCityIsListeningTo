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

    suspend fun getMarkersNearby(markers: List<MapMarker>, userLocation: LatLng, radius: Double): Result<List<MapMarker>> {
        return try {
            val nearbyMarkers = markers.filter { marker ->
                calculateDistance(
                    userLocation.latitude,
                    userLocation.longitude,
                    marker.position.latitude,
                    marker.position.longitude
                ) <= radius
            }
            Result.success(nearbyMarkers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMarkersForZoom(
        markers: List<MapMarker>,
        center: LatLng,
        zoomLevel: Float
    ): Result<List<MapMarker>> {
        return try {
            val radius = calculateRadiusForZoom(zoomLevel)
            val filteredMarkers = markers.filter { marker ->
                calculateDistance(
                    center.latitude,
                    center.longitude,
                    marker.position.latitude,
                    marker.position.longitude
                ) <= radius
            }
            Result.success(filteredMarkers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getMarkersByGenre(
        markers: List<MapMarker>,
        genre: String
    ): Result<List<MapMarker>> {
        return try {
            val filteredMarkers = mutableListOf<MapMarker>() // Lista per accumulare i risultati

            // Itera su ogni marker
            for (marker in markers) {
                val audioId = marker.audioId
                val audioInfo =
                    getAudioInfo(audioId).getOrNull() // Recupera le informazioni sull'audio

                // Filtra per genere
                if (audioInfo != null && audioInfo.tags.genre?.maxByOrNull { it.value }?.key == genre) {
                    filteredMarkers.add(
                        MapMarker(
                            position = LatLng(audioInfo.latitude, audioInfo.longitude),
                            audioId = audioInfo.id
                        )
                    )
                }
            }
            // Ritorna la lista dei marker filtrati
            Result.success(filteredMarkers)
        } catch (e: Exception) {
            Result.failure(e) // Gestione degli errori
        }
    }

    suspend fun getFilteredMarkers(markers: List<MapMarker>, filter: String): Result<List<MapMarker>> {
        return try {
            val filteredMarkers = mutableListOf<MapMarker>() // Lista per accumulare i risultati

            // Itera su ogni marker
            for (marker in markers) {
                val audioId = marker.audioId
                val audioInfo = getAudioInfo(audioId).getOrNull() // Recupera le informazioni sull'audio

                // Verifica se il filtro è contenuto in uno dei campi
                if (audioInfo != null) {
                    val matchesFilter = listOfNotNull(
                        audioInfo.creator_username.contains(filter, ignoreCase = true), // Controlla username
                        audioInfo.tags.genre?.maxByOrNull { it.value }?.key?.contains(filter, ignoreCase = true), // Controlla il genere con il valore maggiore
                        audioInfo.tags.instrument?.maxByOrNull { it.value }?.key?.contains(filter, ignoreCase = true), // Controlla lo strumento con il valore maggiore
                        audioInfo.tags.mood?.maxByOrNull { it.value }?.key?.contains(filter, ignoreCase = true) // Controlla l'umore con il valore maggiore
                    ).any { it } // Verifica se almeno una condizione è vera

                    if (matchesFilter) {
                        filteredMarkers.add(
                            MapMarker(
                                position = LatLng(audioInfo.latitude, audioInfo.longitude),
                                audioId = audioInfo.id
                            )
                        )
                    }
                }
            }
            // Ritorna la lista dei marker filtrati
            Result.success(filteredMarkers)
        } catch (e: Exception) {
            Result.failure(e) // Gestione degli errori
        }
    }


    // Ottieni informazioni specifiche di un audio
    suspend fun getAudioInfo(audioId: Int): Result<AudioResponse> {
        return audioRepository.fetchAudioById(audioId)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // Raggio della Terra in metri
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun calculateRadiusForZoom(zoomLevel: Float): Double {
        return when (zoomLevel.toInt()) {
            in 1..5 -> 500000.0 // Zoom molto lontano: 500 km
            in 6..8 -> 100000.0 // Zoom lontano: 100 km
            in 9..11 -> 50000.0  // Zoom medio: 50 km
            in 12..14 -> 10000.0 // Zoom vicino: 10 km
            in 15..16 -> 5000.0  // Zoom molto vicino: 5 km
            in 17..18 -> 1000.0  // Zoom ravvicinato: 1 km
            else -> 500.0        // Zoom massimo: 500 metri
        }
    }

}
