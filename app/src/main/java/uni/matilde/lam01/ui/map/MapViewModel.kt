package uni.matilde.lam01.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uni.matilde.lam01.ui.audio.AudioViewModel
import java.io.File

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

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission

    fun checkLocationPermission(context: Context) {
        viewModelScope.launch {
            _hasLocationPermission.value = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    // Funzione per caricare i marker vicini alla posizione dell'utente
    fun fetchMarkersForUserLocation(userLocation: LatLng) {
        viewModelScope.launch {
            // Simula il caricamento dei marker vicini alla posizione dell'utente
            val nearbyMarkers = listOf(
                MapMarker(LatLng(userLocation.latitude + 0.01, userLocation.longitude + 0.01), "Brano 1", "Descrizione 1"),
                MapMarker(LatLng(userLocation.latitude - 0.01, userLocation.longitude - 0.01), "Brano 2", "Descrizione 2"),
                MapMarker(LatLng(userLocation.latitude + 0.02, userLocation.longitude + 0.02), "Brano 3", "Descrizione 3")
            )
            _markers.postValue(nearbyMarkers) // Aggiorna i marker in base alla posizione
        }
    }

    fun fetchMarkersForUserLocationAndZoom(userLocation: LatLng, zoomLevel: Float) {
        viewModelScope.launch {
            // Simula marker diversi in base al livello di zoom
            val filteredMarkers = if (zoomLevel > 15) {
                listOf(
                    MapMarker(LatLng(userLocation.latitude + 0.001, userLocation.longitude + 0.001), "Dettaglio 1", "Zoom alto"),
                    MapMarker(LatLng(userLocation.latitude - 0.001, userLocation.longitude - 0.001), "Dettaglio 2", "Zoom alto")
                )
            } else {
                listOf(
                    MapMarker(LatLng(userLocation.latitude + 0.01, userLocation.longitude + 0.01), "Brano 1", "Zoom basso"),
                    MapMarker(LatLng(userLocation.latitude - 0.01, userLocation.longitude - 0.01), "Brano 2", "Zoom basso")
                )
            }
            _markers.postValue(filteredMarkers)
        }
    }


    fun updateUserLocation(location: LatLng) {
        _userLocation.postValue(location) // Aggiorna il valore della posizione utente
    }

}
