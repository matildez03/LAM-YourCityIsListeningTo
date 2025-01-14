package uni.matilde.lam01.ui.audio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository


class UserRecordingsViewModel(private val audioRepository: AudioRepository) : ViewModel() {
    private val _remoteRecordings = MutableLiveData<List<MyAudiosResponse>>()
    val remoteRecordings: LiveData<List<MyAudiosResponse>> get() = _remoteRecordings

    private val _localRecordings = MutableLiveData<List<AudioEntity>>()

    private val _displayedRecordings = MutableLiveData<List<DisplayedAudioInfo>>()
    val displayedRecordings: LiveData<List<DisplayedAudioInfo>> get() = _displayedRecordings

    private val _audioInfo = MutableLiveData<AudioEntity?>()
    val audioInfo: LiveData<AudioEntity?> get() = _audioInfo


    //utilizzo stateflow per notificare ogni aggiornamento del valore
    private val _viewMessage = MutableStateFlow<String?>(null)
    val viewMessage: StateFlow<String?> = _viewMessage


    fun clearMessage() {
        _viewMessage.value = null
    }


    fun getRecordings() {
        viewModelScope.launch {
            try {
                val result = audioRepository.getAllMyLocalAudios().getOrNull()
                if (result != null && !result.isEmpty()) {
                    Log.d("UserRecordingsViewModel", "Audio locali trovati: ${result.size}")
                    _localRecordings.value = result!!

                    val remoteRecordings = audioRepository.fetchMyAudios().getOrNull()
                    Log.d(
                        "UserRecordingsViewModel",
                        "Audio locali trovati: ${remoteRecordings?.size}"
                    )

                    val allRecordingsInfo = mutableListOf<DisplayedAudioInfo>()
                    for (recording in result) {
                        //Debug:
                        Log.d("UserRecordingsViewModel", "ID remoto brano: ${recording.id}")
                        val remoteRecording = remoteRecordings?.find { it.id == recording.id }
                        if (remoteRecording != null) {
                            Log.d(
                                "UserRecordingsViewModel",
                                "ID locale brano: ${remoteRecording.id}"
                            )
                            val displayedRecording = DisplayedAudioInfo(
                                id = recording.id,
                                timestamp = recording.timestamp.toString(),
                                locationName = recording.locationName ?: "",
                                latLng = LatLng(
                                    recording.latitude!!,
                                    recording.longitude!!
                                ),
                                filePath = recording.filePath ?: "",
                                hidden = remoteRecording.hidden
                            )
                            allRecordingsInfo.add(displayedRecording)
                        } else {
                            val displayedRecording = DisplayedAudioInfo(
                                id = recording.id,
                                timestamp = recording.timestamp ?: "Timestamp non trovato",
                                locationName = recording.locationName
                                    ?: "Posizione non riconosciuta",
                                latLng = LatLng(
                                    recording.latitude!!.toDouble(),
                                    recording.longitude!!.toDouble()
                                ),
                                filePath = recording.filePath ?: "",
                                hidden = null
                            )
                            allRecordingsInfo.add(displayedRecording)
                        }
                    }
                    _displayedRecordings.postValue(allRecordingsInfo)
                } else {
                    Log.d("UserRecordingsViewModel", "Non ci sono audio locali dell'utente.")
                }
            } catch (e: Exception) {
                Log.e("UserRecordingsViewModel", "Eccezione durante il fetch: ${e.message}")
                _viewMessage.value = "Errore durante il fetch dei brani"
            }
        }
    }

    
    fun hideRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.hideSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = "Audio nascosto con successo!"

                    // Aggiorna la lista di registrazioni
                    _displayedRecordings.value = _displayedRecordings.value?.map {
                        if (it.id == songId) it.copy(hidden = true) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRecordingsViewModel", "Impossibile nascondere il brano: ${e.message}")
                _viewMessage.value = "Impossibile nascondere il brano"
            }
        }
    }

    fun showRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.showSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = "La registrazione è ora visibile agli utenti!"

                    // Aggiorna la lista di registrazioni
                    _displayedRecordings.value = _displayedRecordings.value?.map {
                        if (it.id == songId) it.copy(hidden = false) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRecordingsViewModel", "Impossibile mostrare il brano: ${e.message}")
                _viewMessage.value = "C'è stato un errore"
            }
        }
    }

    fun deleteRecording(songId: Int) {
        viewModelScope.launch {
            try {
                val result = audioRepository.deleteSong(songId)
                if (result.isSuccess) {
                    _viewMessage.value = "Registrazione eliminata con successo!"

                    _displayedRecordings.value = _displayedRecordings.value?.filter {
                        it.id != songId
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRecordingsViewModel", "Impossibile eliminare il brano: ${e.message}")
                _viewMessage.value = "C'è stato un errore"
            }
        }
    }

    fun showRecordingInfo(songId: Int) {
        viewModelScope.launch {
            try {
                val localResult = audioRepository.getLocalAudioById(songId)
                if (localResult.isSuccess) {
                    if (localResult.getOrNull() != null) {
                        _audioInfo.value = localResult.getOrNull()!!
                    }
                }
                if (!localResult.isSuccess || _audioInfo.value == null) {
                    val remoteResult = audioRepository.fetchAudioById(songId)
                    if (remoteResult.isSuccess) {
                        val audioResponse = remoteResult.getOrNull()
                        val audioEntity = AudioEntity(
                            id = songId,
                            username = "",
                            locationName = "Posizione non trovata",
                            filePath = "",
                            bpm = audioResponse?.tags?.bpm,
                            danceability = audioResponse?.tags?.danceability,
                            loudness = audioResponse?.tags?.loudness,
                            mood = audioResponse?.tags?.mood?.maxByOrNull { it.value }?.key,
                            genre = audioResponse?.tags?.genre?.maxByOrNull { it.value }?.key,
                            instrument = audioResponse?.tags?.instrument?.maxByOrNull { it.value }?.key,
                            latitude = audioResponse?.latitude,
                            longitude = audioResponse?.longitude,
                            timestamp = ""
                        )
                        _audioInfo.value = audioEntity
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "UserRecordingsViewModel",
                    "Impossibile mostrare i dettagli del brano: ${e.message}"
                )
                _viewMessage.value = "C'è stato un errore"
            }
        }
    }

    fun resetAudioInfo() {
        _audioInfo.value = null
    }
}