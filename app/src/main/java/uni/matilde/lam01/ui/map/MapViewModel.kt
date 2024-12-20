package uni.matilde.lam01.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MapMarker(
    val position: LatLng,
    val title: String,
    val description: String
)

class MapViewModel : ViewModel() {

    // LiveData per la posizione dell'utente
    private val _userLocation = MutableLiveData<LatLng?>()
    val userLocation: LiveData<LatLng?> get() = _userLocation

    // LiveData per i marker sulla mappa
    private val _markers = MutableLiveData<List<MapMarker>>()
    val markers: LiveData<List<MapMarker>> get() = _markers

    init {
        fetchMarkers() // Carica i marker iniziali
    }

    fun fetchMarkers() {
        viewModelScope.launch {
            // Simula il caricamento dei marker (puoi sostituire con una chiamata al repository)
            val markerList = listOf(
                MapMarker(LatLng(44.4949, 11.3426), "Canzone 1", "Descrizione della canzone 1"),
                MapMarker(LatLng(45.4642, 9.1900), "Canzone 2", "Descrizione della canzone 2")
            )
            _markers.postValue(markerList) // Aggiorna il valore di LiveData
        }
    }

    fun updateUserLocation(location: LatLng) {
        _userLocation.postValue(location) // Aggiorna il valore della posizione utente
    }
}
