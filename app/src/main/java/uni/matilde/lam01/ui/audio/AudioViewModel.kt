package uni.matilde.lam01.ui.audio

import android.util.Log
import android.widget.Toast
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.math.truncate


class AudioViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _audios = MutableLiveData<List<AudioEntity>>()
    val audios: LiveData<List<AudioEntity>> get() = _audios

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isUploading = MutableLiveData<Boolean>()
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _uploadStatus = MutableLiveData<Boolean?>()
    val uploadStatus: LiveData<Boolean?> get() = _uploadStatus


    fun uploadAudio(file: File, latitude: Double, longitude: Double){
        _isUploading.value = true
        viewModelScope.launch {
            try {
                val requestFile = file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val result = repository.uploadAudio(longitude, latitude, body)
                result.onSuccess { response ->
                    Log.d("AudioViewModel", "Upload completato con successo!")
                    _uploadStatus.postValue(true) // Aggiorna lo stato come successo
                }.onFailure { error ->
                    Log.e("AudioViewModel", "Errore durante l'upload: ${error.message}")
                    _errorMessage.postValue("Errore durante l'upload: ${error.message}")
                    _uploadStatus.postValue(false) // Aggiorna lo stato come fallimento

                }
            } catch (e: Exception) {
                Log.e("AudioViewModel", "Eccezione durante l'upload: ${e.message}")
                _errorMessage.postValue("Errore durante l'upload: ${e.message}")
                _uploadStatus.postValue(false) // Aggiorna lo stato come fallimento
            } finally {
                _isUploading.value = false
            }
        }
    }

    // Funzione per resettare lo stato dell'upload
    fun clearUploadStatus() {
        _uploadStatus.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
