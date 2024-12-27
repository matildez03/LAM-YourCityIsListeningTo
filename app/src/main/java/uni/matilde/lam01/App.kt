package uni.matilde.lam01

import android.app.Application
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.remote.repository.AuthRepository

class App : Application() {

    lateinit var preferencesHelper: PreferencesHelper
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Inizializza PreferencesHelper
        preferencesHelper = PreferencesHelper(this)
        // Inizializza TokenManager
        tokenManager = TokenManager(preferencesHelper)
        // Inizializza AuthRepository
        authRepository = AuthRepository.getInstance(preferencesHelper, tokenManager)

    }

    companion object {
        lateinit var instance: App
            private set
    }
}


