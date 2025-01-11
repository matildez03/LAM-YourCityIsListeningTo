package uni.matilde.lam01.ui.audio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.remote.repository.AudioRepository


class MyRecordingsViewModel(private val audioRepository: AudioRepository) : ViewModel() {
    private val _recordings = MutableLiveData<List<MyAudiosResponse>>()
    val recordings: LiveData<List<MyAudiosResponse>> get() = _recordings

    private val _viewMessage = MutableLiveData<String>()
    val viewMessage: LiveData<String> get() = _viewMessage

    fun getRecordings() {
        viewModelScope.launch {
            try {
                val result = audioRepository.fetchMyAudios().getOrNull()
                if (result != null && !result.isEmpty()) {
                    _recordings.value = result!!
                }
            } catch (e: Exception) {
                Log.e("MyRecordingsViewModel", "Eccezione durante il fetch: ${e.message}")
                _viewMessage.postValue("Errore durante il fetch dei brani")
            }
        }
    }

    fun toggleRecordingVisibility(songId: Int) {
        viewModelScope.launch {
            val song = _recordings.value?.find { it.id == songId }
            if (song?.hidden!!) {
                showRecording(songId)
            } else {
                hideRecording(songId)
            }
        }
    }

    fun hideRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.hideSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = result.getOrNull()?.detail
                }
            } catch (e: Exception) {
                Log.e("MyRecordingsViewModel", "Impossibile nascondere il brano: ${e.message}")
                _viewMessage.postValue("Impossibile nascondere il brano")
            }
        }
    }

    fun showRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.showSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = "La registrazione è ora visibile agli utenti!"
                }
            } catch (e: Exception) {
                Log.e("MyRecordingsViewModel", "Impossibile mostrare il brano: ${e.message}")
                _viewMessage.postValue("C'è stato un errore")
            }
        }
    }

    fun deleteRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.deleteSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = "Registrazione eliminata con successo"
                }
            } catch (e: Exception) {
                Log.e("MyRecordingsViewModel", "Impossibile eliminare il brano: ${e.message}")
                _viewMessage.postValue("C'è stato un errore")
            }
        }
    }
}