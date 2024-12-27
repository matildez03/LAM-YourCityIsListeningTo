package uni.matilde.lam01.api

import retrofit2.Response
import retrofit2.http.*
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.DeleteAccountResponse
import uni.matilde.lam01.data.remote.models.TokenResponse

interface ApiService {
    @POST("auth")
    suspend fun signUp(@Body requestBody: AuthRequest): Response<AuthResponse>

    @FormUrlEncoded
    @POST("auth/token")
    suspend fun getToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>


    @DELETE("auth/unsubscribe")
    suspend fun deleteAccount(@Header("Authentication") token: String): Response<DeleteAccountResponse>

    @GET("audio/all")
    suspend fun getAllSongs(): Response<List<AudioEntity>>

}