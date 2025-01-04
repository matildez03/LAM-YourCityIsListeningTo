import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.ui.map.MapViewModel

class MapViewModelFactory(private val audioRepository: AudioRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(audioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
