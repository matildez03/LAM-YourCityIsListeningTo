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
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.data.remote.repository.MapRepository
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.auth.AuthState
import java.io.File
import java.util.Locale

data class MapMarker(
    val position: LatLng,
    val audioId: Int
)

class MapViewModel(private val mapRepository: MapRepository) : ViewModel() {

    // LiveData per la posizione dell'utente
    private val _userLocation = MutableLiveData<LatLng?>()
    val userLocation: LiveData<LatLng?> get() = _userLocation

    // LiveData per i marker sulla mappa
    private val _allMarkers = MutableLiveData<List<MapMarker>>()

    private val _nearbyMarkers = MutableLiveData<List<MapMarker>>()

    private val _filteredMarkers = MutableLiveData<List<MapMarker>>()

    private val _shownMarkers = MutableLiveData<List<MapMarker>>()
    val shownMarkers: LiveData<List<MapMarker>> get() = _shownMarkers

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> get() = _locationName

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

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
            _locationName.postValue(getLocationName(context, latitude, longitude))
        }
    }

    suspend fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
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
    }


    fun fetchAllMarkers() {
        viewModelScope.launch {
            val result = mapRepository.getMarkers()
            if (result.isSuccess) {
                val tempMarkers = result.getOrNull()
                tempMarkers?.let {
                    _allMarkers.postValue(it)
                }
                Log.d("MapViewModel", "Marker caricati: ${_allMarkers.value?.size ?: 0}")
            } else {
                Log.e("MapViewModel", "Errore nel recupero dei markers")
            }
        }
    }

    fun fetchFilteredMarkers(filter: String) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Mostra il caricamento
            try {
                //filtra solo tra gli audio vicini
                if (_nearbyMarkers.value.isNullOrEmpty()) {
                    fetchAllMarkers()
                }
                val result =
                    mapRepository.getFilteredMarkers(_allMarkers.value!!, filter).getOrNull()
                if (result != null) {
                    _filteredMarkers.postValue(result!!)
                    _shownMarkers.postValue(result!!)
                } else {
                    _errorMessage.postValue("Non ci sono risultati dal tuo filtro!")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Errore durante il caricamento.")
            } finally {
                _isLoading.postValue(false) // Nascondi il caricamento
            }
            Log.d("MapViewModel", "Marker caricati: ${_filteredMarkers.value?.size ?: 0}")
        }
    }


    // Funzione per caricare i marker vicini alla posizione dell'utente
    fun fetchMarkersForUserLocation(userLocation: LatLng, radius: Double) {
        viewModelScope.launch {
            if (_allMarkers.value.isNullOrEmpty()) {
                fetchAllMarkers()
            }
            Log.d("MapViewModel", "Markers totali: ${_allMarkers.value?.size ?: 0}")
            val result =
                mapRepository.getMarkersNearby(_allMarkers.value.orEmpty(), userLocation, radius)
            if (result.isSuccess) {
                val markersResult = result.getOrNull()
                if (markersResult != null) {
                    _nearbyMarkers.postValue(markersResult!!)
                    _shownMarkers.postValue(markersResult!!)
                }
                Log.d("MapViewModel", "Marker caricati: ${_nearbyMarkers.value?.size ?: 0}")
            } else {
                _errorMessage.postValue("Errore nel recupero dei marker vicini.")
                Log.e("MapViewModel", "Errore: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun fetchMarkersForVisibleArea(center: LatLng, zoomLevel: Float) {
        viewModelScope.launch {
            if (_allMarkers.value.isNullOrEmpty()) {
                fetchAllMarkers()
            }
            val allMarkers = _allMarkers.value.orEmpty()
            val result = mapRepository.getMarkersForZoom(allMarkers, center, zoomLevel)
            if (result.isSuccess) {
                val markersResult = result.getOrNull()
                if (markersResult != null) {
                    _nearbyMarkers.postValue(markersResult!!)
                    _shownMarkers.postValue(markersResult!!)
                }
                Log.d(
                    "MapViewModel",
                    "Marker caricati for visible area: ${_nearbyMarkers.value?.size ?: 0}"
                )
            } else {
                _errorMessage.postValue("Errore nel filtraggio dei marker per l'area visibile.")
                Log.e("MapViewModel", "Errore: ${result.exceptionOrNull()?.message}")
            }
        }
    }


    fun updateUserLocation(location: LatLng) {
        _userLocation.postValue(location) // Aggiorna il valore della posizione utente
    }

    suspend fun getAudioInfo(audioId: Int): AudioResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val result = mapRepository.getAudioInfo(audioId)
                if (result.isSuccess) {
                    result.getOrNull()
                } else {
                    Log.e("MapViewModel", "Errore nel recupero dell'audio con id $audioId")
                    null
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Eccezione durante il recupero dell'audio: ${e.message}")
                null
            }
        }
    }

}
