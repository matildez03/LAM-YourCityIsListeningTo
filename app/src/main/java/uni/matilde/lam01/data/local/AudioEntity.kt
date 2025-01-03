package uni.matilde.lam01.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String?,
    val filePath: String,
    val bpm: Int,
    val danceability: Double,
    val loudness: Double,
    val mood: String,
    val genre: String,
    val instrument: String,
    val latitude: Double,
    val longitude: Double
)
