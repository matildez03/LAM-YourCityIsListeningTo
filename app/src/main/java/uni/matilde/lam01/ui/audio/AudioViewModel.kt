package uni.matilde.lam01.ui.audio

import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class AudioViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _audios = MutableLiveData<List<AudioEntity>>()
    val audios: LiveData<List<AudioEntity>> get() = _audios

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchAudios() {
        viewModelScope.launch {
            try {
                val remoteAudios = repository.getAllRemoteAudios()
                _audios.value = remoteAudios
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante il caricamento: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
