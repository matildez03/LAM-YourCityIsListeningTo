package uni.matilde.lam01.core.app

import android.app.Application
import uni.matilde.lam01.data.remote.api.RetrofitInstance
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.local.TokenManager
import uni.matilde.lam01.data.remote.TokenService
import uni.matilde.lam01.data.local.AppDatabase
import uni.matilde.lam01.data.repository.AudioRepository
import uni.matilde.lam01.data.repository.AuthRepository
import uni.matilde.lam01.data.repository.MapRepository
import uni.matilde.lam01.data.remote.UserSessionManager
import uni.matilde.lam01.core.util.NotificationHelper

class App : Application() {

    lateinit var preferencesHelper: PreferencesHelper
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var audioRepository: AudioRepository
        private set

    lateinit var mapRepository: MapRepository
        private set

    lateinit var tokenManager: TokenManager
        private set

    lateinit var tokenService: TokenService
        private set

    lateinit var appDatabase: AppDatabase
        private set

    lateinit var userSessionManager: UserSessionManager
        private set


    override fun onCreate() {
        super.onCreate()

        System.setProperty("flogger.backend_factory", "com.google.common.flogger.backend.system.DefaultPlatform")

        NotificationHelper.createNotificationChannels(this)

        instance = this
        appDatabase = AppDatabase.getDatabase(this)
        preferencesHelper = PreferencesHelper(this)
        tokenManager = TokenManager(preferencesHelper)
        authRepository = AuthRepository.getInstance(RetrofitInstance.api, preferencesHelper, tokenManager)
        tokenService = TokenService(tokenManager, preferencesHelper, authRepository)
        audioRepository = AudioRepository.getInstance(
            apiService = RetrofitInstance.api,
            tokenService = tokenService,
            preferencesHelper = preferencesHelper,
            audioDao = appDatabase.audioDao()
        )
        mapRepository = MapRepository.getInstance(audioRepository = audioRepository)
        userSessionManager = UserSessionManager(preferencesHelper, authRepository, audioRepository, tokenManager)

    }

    companion object {
        lateinit var instance: App
            private set
    }
}


