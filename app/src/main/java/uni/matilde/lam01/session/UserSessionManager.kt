package uni.matilde.lam01.session

import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.data.remote.repository.AuthRepository

class UserSessionManager(
    private val preferencesHelper: PreferencesHelper,
    private val authRepository: AuthRepository,
    private val audioRepository: AudioRepository
) {

    suspend fun onLogout() {
        preferencesHelper.clearPreferences()
    }

    suspend fun onAccountDelete() {
        audioRepository.deleteAllUserRemoteAudios()
        authRepository.deleteAccount()
        preferencesHelper.clearPreferences()
    }
}
