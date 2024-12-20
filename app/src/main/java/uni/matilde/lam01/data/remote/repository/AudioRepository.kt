package uni.matilde.lam01.data.remote.repository

import uni.matilde.lam01.api.RetrofitInstance
import uni.matilde.lam01.data.local.AudioDao
import uni.matilde.lam01.data.local.AudioEntity


class AudioRepository(private val audioDao: AudioDao) {

    suspend fun saveAudioLocally(audio: AudioEntity) {
        audioDao.insert(audio)
    }

    suspend fun getAllLocalAudios(): List<AudioEntity> {
        return audioDao.getAll()
    }
    suspend fun getAllRemoteAudios(): List<AudioEntity> {
        val response = RetrofitInstance.api.getAllSongs()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Errore nella chiamata API: ${response.code()}")
        }
    }
}