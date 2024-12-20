package uni.matilde.lam01.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.TokenResponse
import uni.matilde.lam01.data.remote.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authResult = MutableLiveData<Result<AuthResponse>>()
    val authResult: LiveData<Result<AuthResponse>> = _authResult

    fun signUp(username: String, password: String) {
        viewModelScope.launch {
            _authResult.value = repository.signUp(username, password)
        }
    }

    private val _tokenResult = MutableLiveData<Result<TokenResponse>>()
    val tokenResult: LiveData<Result<TokenResponse>> = _tokenResult

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            _tokenResult.value = repository.getToken(username, password)
        }
    }
}

