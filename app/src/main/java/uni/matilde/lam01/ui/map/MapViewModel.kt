package uni.matilde.lam01.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.auth.AuthState
import java.io.File
import java.util.Locale

data class MapMarker(
    val position: LatLng,
    val audioId: Int
)

class MapViewModel(private val audioRepository: AudioRepository) : ViewModel() {

    // LiveData per la posizione dell'utente
    private val _userLocation = MutableLiveData<LatLng?>()
    val userLocation: LiveData<LatLng?> get() = _userLocation

    // LiveData per i marker sulla mappa
    private val _markers = MutableLiveData<List<MapMarker>>()
    val markers: LiveData<List<MapMarker>> get() = _markers

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> get() = _locationName

    fun checkLocationPermission(context: Context) {
        viewModelScope.launch {
            _hasLocationPermission.value = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /* Funzione per accedere al nome della posizione dell'utente
    Eseguito su contesto i/o perchè si tratta di un' operazione
     bloccante (synchronous API) e può richiedere molto tempo per completarsi
     */
    fun fetchLocationName(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        addresses[0].getAddressLine(0)
                    } else {
                        "Posizione sconosciuta"
                    }
                } catch (e: Exception) {
                    Log.e("GeocoderError", "Errore durante la geocodifica: ${e.message}")
                    "Errore durante la geocodifica"
                }
            }
            _locationName.postValue(name)
        }
    }


    fun fetchMarkers() {
        viewModelScope.launch {
            val result = audioRepository.getAllRemoteAudios()
            if (result.isSuccess) {
                val audios = result.getOrNull()
                Log.d("MapViewModel", "Fetch all audios effettuato con succeso: ${audios?.size}")

                // Mappa ogni audio ricevuto in un oggetto MapMarker
                val tempMarkers = audios?.map { audio ->
                    MapMarker(
                        position = LatLng(audio.latitude, audio.longitude),
                        audioId = audio.id
                    )
                } ?: emptyList()
                _markers.postValue(tempMarkers)// Aggiorna i marker nel LiveData
                Log.d("MapViewModel", "Markers aggiornati")
            } else {
                Log.e("MapViewModel", "Errore nel recupero degli audios")
            }
        }
    }


    // Funzione per caricare i marker vicini alla posizione dell'utente
    fun fetchMarkersForUserLocation(userLocation: LatLng) {
        viewModelScope.launch {
            /*
            // Simula il caricamento dei marker vicini alla posizione dell'utente
            val nearbyMarkers = listOf(
                MapMarker(
                    LatLng(userLocation.latitude + 0.01, userLocation.longitude + 0.01),
                    "Brano 1",
                    "Descrizione 1"
                ),
                MapMarker(
                    LatLng(userLocation.latitude - 0.01, userLocation.longitude - 0.01),
                    "Brano 2",
                    "Descrizione 2"
                ),
                MapMarker(
                    LatLng(userLocation.latitude + 0.02, userLocation.longitude + 0.02),
                    "Brano 3",
                    "Descrizione 3"
                )
            )
            _markers.postValue(nearbyMarkers) // Aggiorna i marker in base alla posizione

             */
        }
    }

    fun fetchMarkersForUserLocationAndZoom(userLocation: LatLng, zoomLevel: Float) {
        viewModelScope.launch {
            /*
            // Simula marker diversi in base al livello di zoom
            val filteredMarkers = if (zoomLevel > 15) {
                listOf(
                    MapMarker(
                        LatLng(userLocation.latitude + 0.001, userLocation.longitude + 0.001),
                        "Dettaglio 1",
                        "Zoom alto"
                    ),
                    MapMarker(
                        LatLng(userLocation.latitude - 0.001, userLocation.longitude - 0.001),
                        "Dettaglio 2",
                        "Zoom alto"
                    )
                )
            } else {
                listOf(
                    MapMarker(
                        LatLng(userLocation.latitude + 0.01, userLocation.longitude + 0.01),
                        "Brano 1",
                        "Zoom basso"
                    ),
                    MapMarker(
                        LatLng(userLocation.latitude - 0.01, userLocation.longitude - 0.01),
                        "Brano 2",
                        "Zoom basso"
                    )
                )
            }
            _markers.postValue(filteredMarkers)

             */
        }
    }


    fun updateUserLocation(location: LatLng) {
        _userLocation.postValue(location) // Aggiorna il valore della posizione utente
    }

}
