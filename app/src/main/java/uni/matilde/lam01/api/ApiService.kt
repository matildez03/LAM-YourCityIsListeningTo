package uni.matilde.lam01.api

import retrofit2.Response
import retrofit2.http.*
import uni.matilde.lam01.data.local.AudioEntity

data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val username: String, val id: Int)
data class TokenResponse(val clientId: Int, val token: String)

interface ApiService {
    @POST("auth")
    suspend fun signUp(@Body requestBody: AuthRequest): Response<AuthResponse>

    @POST("auth/token")
    suspend fun getToken(@Body requestBody: AuthRequest): Response<TokenResponse>

    @GET("audio/all")
    suspend fun getAllSongs(): Response<List<AudioEntity>>
}