package uni.matilde.lam01.ui.audio

import android.media.AudioRecord
import android.media.AudioFormat
import android.media.MediaRecorder
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import kotlin.math.log10

@Composable
fun AudioRecordingDialog(onDismiss: () -> Unit, userLocation: LatLng, audioViewModel: AudioViewModel) {
    val context = LocalContext.current
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Stati per la registrazione
    var isRecording by remember { mutableStateOf(false) }
    var decibelLevel by remember { mutableStateOf(0) }

    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) } // MediaPlayer dichiarato come variabile


    // Stato per sapere se l'utente ha concesso il permesso
    var hasAudioPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher per richiedere il permesso
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasAudioPermission = granted
            if (!granted) {
                Toast.makeText(context, "Permesso per registrare audio negato", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    )



    // Configura AudioRecord
    val audioBuffer = ShortArray(1024)
    val recorder = remember {
        try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBuffer.size * 2
            )
        } catch (e: Exception) {
            Log.e(
                "AudioRecordingDialog",
                "Errore nella configurazione di AudioRecord: ${e.message}"
            )
            null
        }
    }

    // Logica per aggiornare il livello del suono
    LaunchedEffect(isRecording) {
        if (isRecording  && recorder != null && tempFile != null) {
            recorder.startRecording()
            val outputStream = tempFile!!.outputStream()
            try{
            while (isRecording) {
                val result = recorder.read(audioBuffer, 0, audioBuffer.size)
                if (result < 0) {
                    Log.e("AudioRecordingDialog", "Errore durante la lettura dell'audio: $result")
                    break
                }
                // Scrive i dati nel file
                outputStream.write(audioBuffer.toByteArray())
                Log.d("AudioRecordingDialog", "Scrittura su file completata per ${audioBuffer.size} campioni")

                val amplitude = audioBuffer.maxOrNull()?.toInt() ?: 0
                decibelLevel = if (amplitude > 0) (20 * log10(amplitude.toDouble())).toInt() else 0
                delay(100)
            }
            } catch (e: IOException) {
                Log.e("AudioRecordingDialog", "Errore durante la scrittura nel file: ${e.message}")
            } finally {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    Log.e("AudioRecordingDialog", "Errore durante la chiusura del file: ${e.message}")
                }
                recorder.stop()
            }
        }
    }

    val uploadStatus by audioViewModel.uploadStatus.observeAsState()

    LaunchedEffect(uploadStatus) {
        uploadStatus?.let { success ->
            val message = if (success) {
                "Caricamento completato con successo!"
            } else {
                "Errore durante il caricamento dell'audio."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            audioViewModel.clearUploadStatus() // Resetta lo stato dopo aver mostrato il Toast
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registra e Riascolta") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    if (!hasAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@Button
                    }

                    if (!isRecording) {
                        if (recorder == null) {
                            Toast.makeText(context, "Errore nell'inizializzazione del registratore", Toast.LENGTH_SHORT).show()
                            Log.e("AudioRecordingDialog", "Recorder non inizializzato")
                            return@Button
                        }
                        try {
                            tempFile = File.createTempFile("audio_", ".mp3", context.cacheDir)
                            tempFile?.let { recorder?.let {
                                it.startRecording()
                            } }
                            isRecording = true
                            Log.d("AudioRecordingDialog", "File creato: ${tempFile?.absolutePath}")
                            Log.d("AudioRecordingDialog", "Inizio registrazione")
                        } catch (e: IOException) {
                            Toast.makeText(context, "Errore nella creazione del file audio", Toast.LENGTH_SHORT).show()
                            Log.e("AudioRecordingDialog", "Errore nella creazione del file: ${e.message}")
                            isRecording = false
                        } catch (e: Exception){
                            Toast.makeText(context, "Errore generico: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("AudioRecordingDialog", "Errore generico: ${e.message}")
                            isRecording = false
                        }

                    } else {
                        recorder?.let {
                            it.stop()
                        }
                        isRecording = false
                    }
                }) {
                    Text(if (isRecording) "Ferma Registrazione" else "Inizia Registrazione")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val temp = tempFile // Variabile locale per evitare l'errore di smart cast
                    if (temp == null || !temp.exists() || temp.length() == 0L) {
                        Toast.makeText(context, "Il file audio non è disponibile per la riproduzione", Toast.LENGTH_SHORT).show()
                        Log.e("AudioRecordingDialog", "File audio non valido: $tempFile")
                        return@Button //esce dalla lambda di creazione del button
                    }
                    if (isRecording) {
                        recorder?.stop()
                        isRecording = false
                    }
                    try {
                        mediaPlayer?.release() // Rilascia il MediaPlayer precedente se esiste
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(temp.absolutePath)
                            prepare()
                            start()
                        }
                        Log.d("AudioRecordingDialog", "Riproduzione del file: ${temp.absolutePath}")
                    } catch (e: IOException) {
                        Toast.makeText(context, "Errore durante la riproduzione del file audio", Toast.LENGTH_SHORT).show()
                        Log.e("AudioRecordingDialog", "Errore nel MediaPlayer: ${e.message}")
                    } finally {
                        mediaPlayer?.release()
                    }

                }) {
                    Text("Riascolta")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            tempFile?.let { file ->
                Button(onClick = {
                    audioViewModel.clearUploadStatus()
                    userLocation?.let { location ->
                        audioViewModel.uploadAudio(
                            file = file,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        Log.d("AudioViewModel", "Caricamento dell'audio: ${file.name}")

                    } ?: run {
                        Toast.makeText(context, "Posizione non disponibile, impossibile caricare l'audio", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Conferma e Invia")
                }
            } ?: run {
                Toast.makeText(context, "Nessun file disponibile per il caricamento", Toast.LENGTH_SHORT).show()
            }
        }

        ,
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
fun ShortArray.toByteArray(): ByteArray {
    val byteArray = ByteArray(this.size * 2)
    for (i in this.indices) {
        byteArray[i * 2] = (this[i].toInt() and 0xFF).toByte()
        byteArray[i * 2 + 1] = ((this[i].toInt() shr 8) and 0xFF).toByte()
    }
    return byteArray
}
