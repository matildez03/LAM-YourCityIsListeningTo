package uni.matilde.lam01.features.audio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uni.matilde.lam01.data.repository.AudioRepository

class UserRecordingsViewModelFactory(
    private val audioRepository: AudioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserRecordingsViewModel::class.java)) {
            return UserRecordingsViewModel(audioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}