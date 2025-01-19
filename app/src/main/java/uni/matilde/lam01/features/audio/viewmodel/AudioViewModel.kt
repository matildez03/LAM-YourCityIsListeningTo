package uni.matilde.lam01.features.audio.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.repository.AudioRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uni.matilde.lam01.core.util.player.AndroidAudioPlayer
import uni.matilde.lam01.core.util.recorder.AndroidAudioRecorder
import uni.matilde.lam01.core.work.WorkScheduler
import java.io.File


class AudioViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _audios = MutableLiveData<List<AudioEntity>>()
    val audios: LiveData<List<AudioEntity>> get() = _audios

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

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration

    private var timerJob: Job? = null



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

    fun uploadAudio(
        username: String,
        filePath: String,
        latitude: Double,
        longitude: Double,
        locationName: String
    ) {
        viewModelScope.launch {
            try {
                val result = repository.uploadAndSaveAudio(
                    username,
                    filePath,
                    latitude,
                    longitude,
                    locationName
                )
                result.onSuccess {
                    addAudio(it)
                    _uploadStatus.postValue(true) // Aggiorna lo stato come successo
                }.onFailure {
                    _errorMessage.postValue("Errore durante l'upload: ${it.message}")
                    _uploadStatus.postValue(false) // Aggiorna lo stato come fallimento
                }
            } catch (e: Exception) {
                Log.e("AudioViewModel", "Eccezione durante l'upload: ${e.message}")
                _errorMessage.postValue("Errore durante l'upload: ${e.message}")
                _uploadStatus.postValue(false) // Aggiorna lo stato come fallimento
            } finally {
                _mp3AudioPath.value = null
            }
        }
    }

    fun scheduleAudioUpload(
        username: String,
        locationName: String,
        context: Context,
        filePath: String,
        latitude: Double,
        longitude: Double,
        requireWifi: Boolean
    ) {
        try {
            WorkScheduler.scheduleAudioUpload(
                username = username,
                locationName = locationName,
                context = context,
                filePath = filePath,
                latitude = latitude,
                longitude = longitude,
                requireWifi = requireWifi
            )
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Eccezione durante lo scheduling dell'upload: ${e.message}")
            _errorMessage.postValue("Errore durante lo scheduling dell'upload: ${e.message}")
            _uploadStatus.postValue(false) // Aggiorna lo stato come fallimento
        } finally {
            _mp3AudioPath.value = null
        }
    }


    // Funzioni per registrare audio
    fun startRecording(context: Context, userLocation: LatLng, username: String): Boolean {
        val filesDir = context.filesDir
        if (filesDir?.canWrite() == true) {
            val mp4FilePath =
                "${filesDir.absolutePath}/${username}_${userLocation.latitude}_${userLocation.longitude}.mp4"
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
        if (_mp3AudioPath.value != null) {
            val mp3File = _mp3AudioPath.value?.let { File(it) }
            if (mp3File!!.exists()) {
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
                _isRecording.value = false
                return mp3FilePath
            }
        } catch (e: Exception) {
            _errorMessage.value = "Errore durante l'interruzione della registrazione: ${e.message}"
            Log.e(
                "AudioViewModel",
                "Errore durante l'interruzione della registrazione: ${e.message}"
            )
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
            _errorMessage.value =
                "File audio inesistente o vuoto ${audioFile.absolutePath} - lunghezza ${audioFile.length()}"
            Log.e(
                "AudioViewModel",
                "File audio inesistente o vuoto ${audioFile.absolutePath} - lunghezza ${audioFile.length()} - esiste: ${audioFile.exists()}"
            )
        }
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob?.cancel() // Cancella eventuali timer già in esecuzione
        timerJob = viewModelScope.launch {
            _recordingDuration.value = 0
            while (_isRecording.value) {
                delay(1000L)
                _recordingDuration.value = _recordingDuration.value + 1
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel() // Ferma il timer se in esecuzione
        timerJob = null
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
        val ffmpegCommand =
            "-loglevel verbose -i \"$mp4FilePath\" -c:a libmp3lame -qscale:a 2 \"$mp3FilePath\""

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

    fun clearStates() {
        recorder?.stop()
        _isRecording.value = false
        clearError()
        clearUploadStatus()
        _mp3AudioPath.value = null
    }

    fun addAudio(audioEntity: AudioEntity) {
        val currentList = _audios.value ?: emptyList()
        val updatedList = currentList + audioEntity
        _audios.postValue(updatedList)
    }

    fun stopPlayer(){
        player?.stop()
    }

}
