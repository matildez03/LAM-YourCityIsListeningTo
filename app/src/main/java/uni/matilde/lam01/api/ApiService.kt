package uni.matilde.lam01.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.DeleteAccountResponse
import uni.matilde.lam01.data.remote.models.TokenResponse
import uni.matilde.lam01.data.remote.models.UploadAudioResponse

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
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<DeleteAccountResponse>


    //metodi audio

    @Multipart
    @POST("upload")
    suspend fun uploadAudio(
        @Header("Authorization") token: String,
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Part file: MultipartBody.Part
    ): Response<UploadAudioResponse>

    @GET("audio/all")
    suspend fun getAllSongs(): Response<List<AudioEntity>>

}