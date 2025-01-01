package uni.matilde.lam01.data.remote.models

import com.google.gson.annotations.SerializedName


data class UploadAudioResponse (
    @SerializedName("bpm")
    val bpm: Int,

    @SerializedName("danceability")
    val danceability: Double,

    @SerializedName("loudness")
    val loudness: Double,

    @SerializedName("mood")
    val mood: Map<String, Double>,

    @SerializedName("genre")
    val genre: Map<String, Double>,

    @SerializedName("instrument")
    val instrument: Map<String, Double>
)