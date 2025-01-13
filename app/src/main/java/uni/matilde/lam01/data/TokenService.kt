package uni.matilde.lam01.data

import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.repository.AuthRepository

class TokenService(
    private val tokenManager: TokenManager,
    private val preferencesHelper: PreferencesHelper,
    private val authRepository: AuthRepository
) {
    suspend fun getValidToken(): String? {
        return tokenManager.getToken() ?: run {
            val username = preferencesHelper.getUsername()
            val password = preferencesHelper.getPassword()
            if (username != null && password != null) {
                val result = authRepository.getToken(username, password)
                if (result.isSuccess) {
                    result.getOrNull()?.let {
                        tokenManager.setToken(it.client_secret)
                        preferencesHelper.saveToken(it.client_secret)
                        return it.client_secret
                    }
                }
            }
            null
        }
    }
}
