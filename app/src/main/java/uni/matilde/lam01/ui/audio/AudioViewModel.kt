package uni.matilde.lam01.ui.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    //PERMESSI
    // RECORD_AUDIO
    private val _hasAudiorecordPermission = MutableStateFlow(false)
    val hasAudiorecordPermission: StateFlow<Boolean> = _hasAudiorecordPermission

    // WRITE_EXTERNAL_STORAGE
    private val _hasWriteExPermission = MutableStateFlow(false)
    val hasWriteExPermission: StateFlow<Boolean> = _hasWriteExPermission

    // READ_EXTERNAL_STORAGE
    private val _hasReadExPermission = MutableStateFlow(false)
    val hasReadExPermission: StateFlow<Boolean> = _hasReadExPermission

    fun checkRecordAudioPermission(context: Context) {
        _hasAudiorecordPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkWriteExternalStoragePermission(context: Context) {
        _hasWriteExPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkReadExternalStoragePermission(context: Context) {
        _hasReadExPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


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
