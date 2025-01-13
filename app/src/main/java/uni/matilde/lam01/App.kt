package uni.matilde.lam01

import android.app.Application
import uni.matilde.lam01.api.RetrofitInstance
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.TokenManager
import uni.matilde.lam01.data.local.AppDatabase
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.data.remote.repository.AuthRepository
import uni.matilde.lam01.data.remote.repository.MapRepository
import uni.matilde.lam01.util.NotificationHelper
import uni.matilde.lam01.util.recorder.AndroidAudioRecorder

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

    lateinit var appDatabase: AppDatabase
        private set


    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannels(this)


        instance = this
        // Inizializza PreferencesHelper
        preferencesHelper = PreferencesHelper(this)
        // Inizializza TokenManager
        tokenManager = TokenManager(preferencesHelper)

        // Inizializza il Database
        appDatabase = AppDatabase.getDatabase(this)

        // Inizializza AuthRepository
        authRepository = AuthRepository.getInstance(preferencesHelper, tokenManager, RetrofitInstance.api)

        audioRepository = AudioRepository.getInstance(
            apiService = RetrofitInstance.api,
            tokenManager = tokenManager,
            preferencesHelper = preferencesHelper,
            audioDao = appDatabase.audioDao()
        )

        mapRepository = MapRepository.getInstance(audioRepository = audioRepository)

    }

    companion object {
        lateinit var instance: App
            private set
    }
}


