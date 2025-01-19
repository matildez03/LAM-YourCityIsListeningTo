package uni.matilde.lam01.core.util.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun startRecording(outputFile: File) {
        //configurazione del registratore
        // Verifica che il file sia valido
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }

        if (!outputFile.canWrite()) {
            throw IllegalStateException("Il file di output non è scrivibile: ${outputFile.absolutePath}")
        }
        try {
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                recorder = this
                start()
                Log.d("AndroidAudioRecorder", "Registrazione iniziata su ${outputFile.absolutePath}")
            }
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            Log.d("AndroidAudioRecorder", "Errore durante la registrazione audio: ${e.message}")
            throw RuntimeException("Errore durante la registrazione audio: ${e.message}", e)
        }
    }

    override fun stop(){
        try {
            recorder?.apply {
                stop()
                release()
            }
            Log.d("AndroidAudioRecorder", "Registrazione interrotta")
        } catch (e: Exception) {
            // Ignora l'errore se il recorder non è stato avviato correttamente
        } finally {
            recorder?.release()
            recorder = null
        }
    }


}