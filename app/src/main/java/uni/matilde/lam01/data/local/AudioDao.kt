package uni.matilde.lam01.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import uni.matilde.lam01.data.remote.repository.AudioRepository
data class MoodCount(
    val mood: String,
    val count: Int
)

data class GenreCount(
    val genre: String,
    val count: Int
)

@Dao
interface AudioDao {
    @Insert
    suspend fun insert(audio: AudioEntity)

    @Query("SELECT * FROM audio")
    suspend fun getAll(): List<AudioEntity>

    @Query("SELECT * FROM audio WHERE username= :username")
    suspend fun getAllByUsername(username: String): List<AudioEntity>

    @Query("SELECT * FROM audio WHERE id = :audioId")
    suspend fun getById(audioId: Int): AudioEntity

    @Query("DELETE FROM audio WHERE username = :username")
    suspend fun deleteByUsername(username: String)

    @Query("SELECT COUNT(*) FROM audio WHERE username = :username")
    fun getAudioCount(username: String): Int

    @Query("SELECT AVG(bpm) FROM audio WHERE bpm IS NOT NULL AND username = :username")
    fun getAverageBpm(username: String): Double?

    @Query("SELECT mood, COUNT(*) as count FROM audio WHERE username = :username GROUP BY mood ")
    fun getMoodDistribution(username: String): List<MoodCount>

    @Query("SELECT genre, COUNT(*) as count FROM audio WHERE username = :username GROUP BY genre")
    fun getGenreDistribution(username: String): List<GenreCount>
}
