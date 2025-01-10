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
    private val _markers = MutableLiveData<List<MapMarker>>()
    val markers: LiveData<List<MapMarker>> get() = _markers

    private val _filteredMarkers = MutableLiveData<List<MapMarker>>()
    val filteredMarkers: LiveData<List<MapMarker>> get() = _filteredMarkers

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    private val _locationName = MutableLiveData<String>()
    val locationName: LiveData<String> get() = _locationName

    private val _audio = MutableLiveData<AudioResponse>()
    val audio: LiveData<AudioResponse> get() = _audio

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

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
            _locationName.postValue(getLocationName(context,latitude,longitude))
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
                    _markers.postValue(it)
                }
                Log.d("MapViewModel", "Marker caricati: ${_markers.value?.size ?: 0}")
            } else {
                Log.e("MapViewModel", "Errore nel recupero dei markers")
            }
        }
    }

    fun fetchMarkersByGenre(genre: String){
        viewModelScope.launch {
            if(_markers != null){
                val result = mapRepository.getMarkersByGenre(markers.value!!, genre).getOrNull()
                if(result !=null) {
                    _markers.postValue(result!!)
                }
                else{
                    _errorMessage.postValue("Non ci sono risultati dal tuo filtro!")
                }
                Log.d("MapViewModel", "Marker caricati: ${_markers.value?.size ?: 0}")
            } else{
                Log.e("MapViewModel","Impossibile trovare i markers filtrati: val markers is null")
            }
        }
    }

    fun fetchFilteredMarkers(filter: String){
        viewModelScope.launch {
            if(_markers != null){
                val result = mapRepository.getFilteredMarkers(markers.value!!, filter).getOrNull()
                if(result !=null) {
                    _markers.postValue(result!!)
                }
                else{
                    _errorMessage.postValue("Non ci sono risultati dal tuo filtro!")
                }
                Log.d("MapViewModel", "Marker caricati: ${_markers.value?.size ?: 0}")
            } else{
                Log.e("MapViewModel","Impossibile trovare i markers filtrati: val markers is null")
            }
        }
    }


    // Funzione per caricare i marker vicini alla posizione dell'utente
    fun fetchMarkersForUserLocation(userLocation: LatLng, radius: Double) {
        viewModelScope.launch {
            val result = mapRepository.getMarkersNearby(_markers.value.orEmpty(), userLocation, radius)
            if (result.isSuccess) {
                val markersResult = result.getOrNull()
                if(markersResult != null) {
                    _markers.postValue(markersResult!!)
                }
                Log.d("MapViewModel", "Marker caricati: ${_markers.value?.size ?: 0}")
            } else {
                _errorMessage.postValue("Errore nel recupero dei marker vicini.")
                Log.e("MapViewModel", "Errore: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun fetchMarkersForVisibleArea(center: LatLng, zoomLevel: Float) {
        viewModelScope.launch {
            val allMarkers = _markers.value.orEmpty()
            val result = mapRepository.getMarkersForZoom(allMarkers, center, zoomLevel)
            if (result.isSuccess) {
                val markersResult = result.getOrNull()
                if(markersResult != null) {
                    _markers.postValue(markersResult!!)
                }
                Log.d("MapViewModel", "Marker caricati: ${_markers.value?.size ?: 0}")
            } else {
                _errorMessage.postValue("Errore nel filtraggio dei marker per l'area visibile.")
                Log.e("MapViewModel", "Errore: ${result.exceptionOrNull()?.message}")
            }
        }
    }






    fun updateUserLocation(location: LatLng) {
        _userLocation.postValue(location) // Aggiorna il valore della posizione utente
    }

    fun getAudioInfo(audioId: Int) {
        viewModelScope.launch {
            val result = mapRepository.getAudioInfo(audioId)
            if (result.isSuccess) {
                val tempAudio = result.getOrNull()
                tempAudio?.let {
                    _audio.postValue(it)
                }
            } else {
                Log.e("MapViewModel", "Errore nel recupero dell'audio con id $audioId")
            }
        }
    }

}
