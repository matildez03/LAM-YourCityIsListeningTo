package uni.matilde.lam01.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.repository.AuthRepository

// Stato sigillato per rappresentare i vari stati del processo di autenticazione
sealed class AuthState {
    object Idle : AuthState() // Stato inattivo o neutro
    object Loading : AuthState()
    data class Success<T>(val data: T) : AuthState()
    data class Error(val message: String?) : AuthState()
}
class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    // MutableLiveData privata per gestire lo stato internamente
    private val _authState = MutableLiveData<AuthState?>()
    val authState: LiveData<AuthState?> = _authState


    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Funzione per gestire il login
    fun login(username: String, password: String) {
        viewModelScope.launch {
            // Imposta lo stato su Loading prima di iniziare l'operazione
            _authState.value = AuthState.Loading
            // Chiamata al repository per ottenere il token
            val result = repository.getToken(username, password)
            // Gestione esplicita di successo ed errore
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message)
            }
        }
    }

    // Funzione per la registrazione
    fun signUp(username: String, password: String) {
        viewModelScope.launch {
            // Imposta lo stato su Loading prima di iniziare l'operazione
            _authState.value = AuthState.Loading
            // Chiamata al repository per registrare l'utente
            val result = repository.signUp(username, password)
            // Gestione esplicita di successo ed errore
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteAccount() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                Log.d("Delete","richiesta di eliminazione ricevuta")
                val response = repository.deleteAccount()
                if (response.isSuccess) {
                    _authState.value = AuthState.Success("Account eliminato con successo.")
                    Log.d("Delete","Account eliminato con successo.");
                } else {
                    _authState.value = AuthState.Error("Errore durante l'eliminazione dell'account.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message)
            }
        }
    }

    // Funzione per reimpostare lo stato (opzionale)
    fun resetAuthState() {
        _authState.value = null
    }
}
