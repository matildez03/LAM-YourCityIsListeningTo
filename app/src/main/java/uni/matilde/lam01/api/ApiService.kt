package uni.matilde.lam01.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.models.AllAudiosResponse
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.data.remote.models.AuthRequest
import uni.matilde.lam01.data.remote.models.AuthResponse
import uni.matilde.lam01.data.remote.models.DeleteAccountResponse
import uni.matilde.lam01.data.remote.models.DetailResponse
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
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
    suspend fun getAllSongs(@Header("Authorization") token: String): Response<List<AllAudiosResponse>>

    @GET("audio/{audioId}")
    suspend fun getAudioById(@Header("Authorization") token: String, @Path("audioId") audioId: Int): Response<AudioResponse>

    @GET("audio/my")
    suspend fun getMySongs(@Header("Authorization") token: String): Response<List<MyAudiosResponse>>

    @GET("audio/my/{song_id}/hide")
    suspend fun hideSong(@Header("Authorization") token: String, @Path("song_id") songId: Int): Response<DetailResponse>

    @GET("audio/my/{song_id}/show")
    suspend fun showSong(@Header("Authorization") token: String, @Path("song_id") songId: Int): Response<MyAudiosResponse>

    @DELETE("audio/{song_id}")
    suspend fun deleteSong(@Header("Authorization") token: String, @Path("song_id") songId: Int): Response<DetailResponse>

}