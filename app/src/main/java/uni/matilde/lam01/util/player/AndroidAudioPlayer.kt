package uni.matilde.lam01.util.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.IOException

class AndroidAudioPlayer(
    private val context: Context

) : AudioPlayer {

    private var player: MediaPlayer? = null

    override fun playFile(file: File) {

        // Rilascia qualsiasi player esistente prima di crearne uno nuovo
        if (player != null) {
            try {
                player?.stop()
            } catch (e: Exception) {
                Log.e(
                    "AndroidAudioPlayer",
                    "Errore durante lo stop del MediaPlayer precedente: ${e.message}"
                )
            } finally {
                player?.release()
                player = null
            }
        }

        try {
            if (!file.exists() || !file.canRead()) {
                throw IOException("Il file non esiste o non è leggibile: ${file.absolutePath}")
            }

            if (!file.canRead()) {
                throw IOException("Il file non è leggibile: ${file.absolutePath}")
            }
            //debug
            Log.d(
                "AndroidAudioPlayer",
                "File esiste ed è leggibile: ${file.absolutePath}, Dimensione: ${file.length()} bytes"
            )


            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnErrorListener { _, what, extra ->
                    Log.e(
                        "AndroidAudioPlayer",
                        "Errore durante la riproduzione: what=$what, extra=$extra"
                    )
                    releasePlayer()
                    true
                }
                setOnCompletionListener {
                    Log.d("AndroidAudioPlayer", "Riproduzione completata.")
                    releasePlayer()
                }
                prepare() // Prepara il file per la riproduzione
                start()
            }
        } catch (e: IOException) {
            Log.e(
                "AndroidAudioPlayer",
                "IOEXCEPTION: Errore durante la configurazione del MediaPlayer: ${e.message}"
            )
            throw RuntimeException(
                "Errore durante la configurazione del MediaPlayer: ${e.message}",
                e
            )
        } catch (e: IllegalStateException) {
            Log.e("AndroidAudioPlayer", "Stato non valido del MediaPlayer: ${e.message}")
            throw RuntimeException("Stato non valido del MediaPlayer: ${e.message}", e)
        } finally {
            releasePlayer()
        }
    }

    override fun stop() {
        try {
            player?.stop()
        } catch (e: Exception) {
            Log.e("AndroidAudioPlayer", "Errore durante lo stop del MediaPlayer: ${e.message}")
        } finally {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

}