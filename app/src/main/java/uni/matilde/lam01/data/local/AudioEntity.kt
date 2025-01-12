package uni.matilde.lam01.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey()
    val id: Int,
    val username: String?,
    val locationName: String?,
    val filePath: String?,
    val bpm: Int?,
    val danceability: Double?,
    val loudness: Double?,
    val mood: String?,
    val genre: String?,
    val instrument: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: String? // ver 4
)

//Rimozione di auto_generated: necessario ricreare la tabella
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Crea una nuova tabella con il nuovo schema
        database.execSQL("""
            CREATE TABLE audio_new (
                id INTEGER PRIMARY KEY NOT NULL, 
                username TEXT,
                filePath TEXT,
                bpm INTEGER,
                danceability REAL,
                loudness REAL,
                mood TEXT,
                genre TEXT,
                instrument TEXT,
                latitude REAL,
                longitude REAL
            )
        """)

        // 2. Copia i dati dalla vecchia tabella alla nuova
        database.execSQL("""
            INSERT INTO audio_new (id, username,filePath, bpm, danceability, loudness, mood, genre, instrument, latitude, longitude)
            SELECT id, username,filePath, bpm, danceability, loudness, mood, genre, instrument, latitude, longitude
            FROM audio
        """)

        // 3. Elimina la vecchia tabella
        database.execSQL("DROP TABLE audio")

        // 4. Rinomina la nuova tabella al nome originale
        database.execSQL("ALTER TABLE audio_new RENAME TO audio")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE audio ADD COLUMN locationName TEXT")
    }
}



val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Aggiungi la colonna "timestamp" alla tabella esistente
        database.execSQL("ALTER TABLE audio ADD COLUMN timestamp TEXT")
    }
}
