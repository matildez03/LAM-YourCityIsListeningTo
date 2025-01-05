package uni.matilde.lam01.data.remote.models

data class AudioResponse(
    val longitude: Double,
    val latitude: Double,
    val id: Int,
    val creator_id: Int,
    val creator_username: String,
    val tags: Tags
)

data class Tags(
    val bpm: Int?,
    val danceability: Double?,
    val loudness: Double?,
    val mood: Map<String, Double>?,
    val genre: Map<String, Double>?,
    val instrument: Map<String, Double>?
)