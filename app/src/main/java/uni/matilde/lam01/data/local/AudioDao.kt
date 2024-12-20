package uni.matilde.lam01.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AudioDao {
    @Insert
    suspend fun insert(audio: AudioEntity)

    @Query("SELECT * FROM audio")
    suspend fun getAll(): List<AudioEntity>
}