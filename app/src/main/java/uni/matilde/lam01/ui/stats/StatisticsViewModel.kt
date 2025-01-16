package uni.matilde.lam01.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.repository.AudioRepository

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.local.GenreCount
import uni.matilde.lam01.data.local.MoodCount

class StatisticsViewModel(private val repository: AudioRepository) : ViewModel() {

    private val _totalAudios = MutableLiveData<Result<Int>>()
    val totalAudios: LiveData<Result<Int>> get() = _totalAudios

    private val _averageBpm = MutableLiveData<Result<Double>>()
    val averageBpm: LiveData<Result<Double>> get() = _averageBpm

    private val _moodDistribution = MutableLiveData<Result<List<MoodCount>>>()
    val moodDistribution: LiveData<Result<List<MoodCount>>> get() = _moodDistribution

    private val _genreDistribution = MutableLiveData<Result<List<GenreCount>>>()
    val genreDistribution: LiveData<Result<List<GenreCount>>> get() = _genreDistribution

    fun getTotalAudios() {
        viewModelScope.launch {
            try {
                val result = repository.getAudioCount()
                _totalAudios.value = result
            } catch (e: Exception) {
                _totalAudios.value = Result.failure(e)
            }
        }
    }

    fun getAverageBpm() {
        viewModelScope.launch {
            try {
                val result = repository.getAverageBpm()
                _averageBpm.value = result
            } catch (e: Exception) {
                _averageBpm.value = Result.failure(e)
            }
        }
    }

    fun getMoodDistribution() {
        viewModelScope.launch {
            try {
                val result = repository.getMoodDistribution()
                _moodDistribution.value = result
            } catch (e: Exception) {
                _moodDistribution.value = Result.failure(e)
            }
        }
    }

    fun getGenreDistribution() {
        viewModelScope.launch {
            try {
                val result = repository.getGenreDistribution()
                _genreDistribution.value = result
            } catch (e: Exception) {
                _genreDistribution.value = Result.failure(e)
            }
        }
    }
}

