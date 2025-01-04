package uni.matilde.lam01.ui.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uni.matilde.lam01.data.remote.models.UploadAudioResponse
import uni.matilde.lam01.util.player.AndroidAudioPlayer
import uni.matilde.lam01.util.recorder.AndroidAudioRecorder
import java.io.File
import java.util.Locale
import kotlin.math.truncate


class AudioViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _audios = MutableLiveData<List<AudioEntity>>()
    val audios: LiveData<List<AudioEntity>> get() = _audios

    private val _isUploading = MutableLiveData<Boolean>()
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _uploadStatus = MutableLiveData<Boolean?>()
    val uploadStatus: LiveData<Boolean?> get() = _uploadStatus

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

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


    //REGISTRAZIONE AUDIO E RIASCOLTO
    private var recorder: AndroidAudioRecorder? = null
    private var player: AndroidAudioPlayer? = null

    private val _currentRecordingPath = MutableStateFlow<String?>(null)
    val currentRecordingPath: StateFlow<String?> = _currentRecordingPath

    private val _mp3AudioPath = MutableStateFlow<String?>(null)
    val mp3AudioPath: StateFlow<String?> = _mp3AudioPath

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording


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


    fun uploadAudio(username: String, filePath: String, latitude: Double, longitude: Double, locationName: String){
        _isUploading.value = true
        viewModelScope.launch {
            try {
                val file = File(filePath)
                val requestFile = file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val result = repository.uploadAudio(longitude, latitude, body)
                result.onSuccess { audioResponse ->
                    Log.d("AudioViewModel", "Upload completato con successo!")
                    _uploadStatus.postValue(true) // Aggiorna lo stato come successo
                    Log.d("AudioViewModel", audioResponse.toString())


                    // Salva in locale
                    val audioEntity = AudioEntity(
                        username = username,
                        locationName = locationName,
                        filePath = filePath,
                        bpm = audioResponse.bpm,
                        danceability = audioResponse.danceability,
                        loudness = audioResponse.loudness,
                        mood = audioResponse.mood.maxByOrNull { it.value }?.key,
                        genre = audioResponse.genre.maxByOrNull { it.value }?.key,
                        instrument = audioResponse.instrument.maxByOrNull { it.value }?.key,
                        latitude = latitude,
                        longitude = longitude
                    )

                    repository.saveAudioLocally(audioEntity)
                    addAudio(audioEntity)

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



    // Funzioni per registrare audio
    fun startRecording(context: Context, userLocation: LatLng, username: String): Boolean {
        val filesDir = context.filesDir
        if (filesDir?.canWrite() == true) {
            val mp4FilePath = "${filesDir.absolutePath}/${username}_${userLocation.latitude}_${userLocation.longitude}.mp4"
            val mp4File = File(mp4FilePath)

            // Verifica se il file esiste già e lo cancella
            if (mp4File.exists()) {
                Log.d("AudioViewModel", "Il file $mp4FilePath esiste già. Eliminazione in corso...")
                val deleted = mp4File.delete()
                if (!deleted) {
                    _errorMessage.postValue("Errore: impossibile eliminare il file esistente.")
                    Log.e("AudioViewModel", "Errore: impossibile eliminare il file $mp4FilePath")
                    return false
                }
            }

            deletePreviousMp3()

            try {
                recorder = AndroidAudioRecorder(context)
                recorder?.startRecording(mp4File)
                _currentRecordingPath.value = mp4FilePath
                _isRecording.value = true // Aggiorna lo stato
                Log.d("AudioViewModel", "Registrazione iniziata su: $mp4FilePath")
                return true
            } catch (e: Exception) {
                _errorMessage.value = "Errore nella registrazione: ${e.message}"
                Log.e("AudioViewModel", "Errore nella registrazione: ${e.message}")
            }
        } else {
            _errorMessage.value = "Directory non scrivibile: $filesDir"
            Log.e("AudioViewModel", "Directory non scrivibile: $filesDir")
        }
        return false
    }

    private fun deletePreviousMp3() {
        //cancella eventuali audio scartati
        if(_mp3AudioPath.value != null) {
            val mp3File = _mp3AudioPath.value?.let { File(it) }
            if (mp3File!!.exists()) {
                Log.d(
                    "AudioViewModel",
                    "Eliminazione del file precedentemente scartato in corso..."
                )
                val deleted = mp3File.delete()
                if (!deleted) {
                    _errorMessage.postValue("Errore: impossibile eliminare il file esistente.")
                    Log.e(
                        "AudioViewModel",
                        "Errore: impossibile eliminare il file ${_mp3AudioPath.value}"
                    )
                } else {
                    Log.d("AudioViewModel", "File scartato eliminato.")
                }
            }
            _mp3AudioPath.value = null //reset
        }
    }

    fun stopRecording(context: Context): String? {
        try {
            recorder?.stop()
            val mp4FilePath = _currentRecordingPath.value
            if (mp4FilePath != null) {
                val mp3FilePath = mp4FilePath.replace(".mp4", ".mp3")
                convertToMp3(mp4FilePath, mp3FilePath, context)
                _mp3AudioPath.value = mp3FilePath
                _isRecording.value = false // Aggiorna lo stato
                Log.d("AudioViewModel", "Registrazione interrotta su: $mp4FilePath")
                return mp3FilePath
            }
        } catch (e: Exception) {
            _errorMessage.value = "Errore durante l'interruzione della registrazione: ${e.message}"
            Log.e("AudioViewModel", "Errore durante l'interruzione della registrazione: ${e.message}")
        }
        return null
    }

    fun playRecording(context: Context, filePath: String) {
        val audioFile = File(filePath)
        if (audioFile.exists() && audioFile.length() > 0) {
            try {
                player = AndroidAudioPlayer(context)
                player?.playFile(audioFile)
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante la riproduzione: ${e.message}"
                Log.e("AudioViewModel", "Errore durante la riproduzione: ${e.message}")
            }
        } else {
            _errorMessage.value = "File audio inesistente o vuoto ${audioFile.absolutePath} - lunghezza ${audioFile.length()}"
            Log.e("AudioViewModel", "File audio inesistente o vuoto ${audioFile.absolutePath} - lunghezza ${audioFile.length()} - esiste: ${audioFile.exists()}")
        }
    }

    private fun convertToMp3(mp4FilePath: String, mp3FilePath: String, context: Context) {

        val mp3File = File(mp3FilePath)

        // Verifica se il file MP3 esiste già e lo cancella
        if (mp3File.exists()) {
            Log.d("AudioViewModel", "Il file MP3 $mp3FilePath esiste già. Eliminazione in corso...")
            val deleted = mp3File.delete()
            if (!deleted) {
                _errorMessage.postValue("Errore: impossibile eliminare il file MP3 esistente.")
                Log.e("AudioViewModel", "Errore: impossibile eliminare il file MP3 $mp3FilePath")
                return
            }
        }
        val ffmpegCommand = "-loglevel verbose -i \"$mp4FilePath\" -c:a libmp3lame -qscale:a 2 \"$mp3FilePath\""

        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Log.d("AudioViewModel", "Conversione a MP3 completata con successo.")
                val mp4File = File(mp4FilePath)
                if (mp4File.exists()) {
                    mp4File.delete()
                    Log.d("AudioViewModel", "File MP4 eliminato con successo.")
                }
            } else {
                val failStackTrace = session.failStackTrace ?: "Errore sconosciuto"
                _errorMessage.value = "Errore durante la conversione a MP3: $failStackTrace"
                Log.e("AudioViewModel", "Errore durante la conversione a MP3: $failStackTrace")
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

    fun clearStates(){
        recorder?.stop()
        _isRecording.value = false
        clearError()
        clearUploadStatus()
        deletePreviousMp3()
    }

    fun addAudio(audioEntity: AudioEntity) {
        // Recupera la lista corrente o una lista vuota se è null
        val currentList = _audios.value ?: emptyList()

        // Crea una nuova lista aggiungendo l'elemento
        val updatedList = currentList + audioEntity

        // Aggiorna il valore di _audios
        _audios.postValue(updatedList)
    }

}
