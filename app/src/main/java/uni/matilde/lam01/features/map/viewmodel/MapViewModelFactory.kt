package uni.matilde.lam01.features.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uni.matilde.lam01.data.repository.MapRepository

class MapViewModelFactory(private val mapRepository: MapRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(mapRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
