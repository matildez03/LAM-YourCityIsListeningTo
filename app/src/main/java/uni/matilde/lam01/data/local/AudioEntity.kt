package uni.matilde.lam01.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey val id: Int,
    val longitude: Float,
    val latitude: Float,
    val tags: String
)
