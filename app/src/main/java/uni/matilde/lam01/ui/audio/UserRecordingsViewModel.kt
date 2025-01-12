package uni.matilde.lam01.ui.audio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.remote.repository.AudioRepository


class UserRecordingsViewModel(private val audioRepository: AudioRepository) : ViewModel() {
    private val _remoteRecordings = MutableLiveData<List<MyAudiosResponse>>()
    val remoteRecordings: LiveData<List<MyAudiosResponse>> get() = _remoteRecordings

    private val _displayedRecordings = MutableLiveData<List<DisplayedAudioInfo>>()
    val displayedRecordings: LiveData<List<DisplayedAudioInfo>> get() = _displayedRecordings

    private val _viewMessage = MutableLiveData<String>()
    val viewMessage: LiveData<String> get() = _viewMessage

    fun getRecordings() {
        viewModelScope.launch {
            try {
                val result = audioRepository.fetchMyAudios().getOrNull()
                if (result != null && !result.isEmpty()) {
                    Log.d("UserRecordingsViewModel","Audio remoti trovati: ${result.size}")
                    _remoteRecordings.value = result!!

                    val localRecordings = audioRepository.getAllLocalAudios().getOrNull()
                    Log.d("UserRecordingsViewModel","Audio locali trovati: ${localRecordings?.size}")

                    val allRecordingsInfo = mutableListOf<DisplayedAudioInfo>()
                    for (recording in result) {
                        //Debug:
                        Log.d("UserRecordingsViewModel","ID remoto brano: ${recording.id}")
                        val localRecording = localRecordings?.find { it.id == recording.id }
                        if(localRecording != null){
                            Log.d("UserRecordingsViewModel","ID locale brano: ${localRecording.id}")
                            val displayedRecording = DisplayedAudioInfo(
                            id=  recording.id,
                            timestamp= localRecording?.timestamp.toString(),
                            locationName= localRecording?.locationName?:"",
                            latLng= LatLng(localRecording?.latitude!!, localRecording.longitude!!),
                            filePath=localRecording?.filePath?:"",
                            hidden= recording.hidden
                        )
                            allRecordingsInfo.add(displayedRecording)
                        }
                        else{
                            val displayedRecording = DisplayedAudioInfo(
                                id=  recording.id,
                                timestamp= "timestamp non trovato",
                                locationName= "Posizione non riconosciuta",
                                latLng=  LatLng(recording.latitude!!.toDouble(), recording.longitude!!.toDouble()),
                                filePath="",
                                hidden= recording.hidden
                            )
                            allRecordingsInfo.add(displayedRecording)
                        }
                    }

                    //debug
                    for(rec in localRecordings!!){
                        Log.d("UserRecordingsViewModel","${rec.id}")
                    }
                    _displayedRecordings.postValue(allRecordingsInfo)
                } else{
                    Log.d("UserRecordingsViewModel","Non ci sono audio remoti dell'utente.")
                }
            } catch (e: Exception) {
                Log.e("UserRecordingsViewModel", "Eccezione durante il fetch: ${e.message}")
                _viewMessage.postValue("Errore durante il fetch dei brani")
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
                Log.e("UserRecordingsViewModel", "Impossibile nascondere il brano: ${e.message}")
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
                Log.e("UserRecordingsViewModel", "Impossibile mostrare il brano: ${e.message}")
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
                Log.e("UserRecordingsViewModel", "Impossibile eliminare il brano: ${e.message}")
                _viewMessage.postValue("C'è stato un errore")
            }
        }
    }
}