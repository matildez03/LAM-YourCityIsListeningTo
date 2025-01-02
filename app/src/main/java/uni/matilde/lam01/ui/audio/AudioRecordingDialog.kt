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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import uni.matilde.lam01.util.player.AndroidAudioPlayer
import uni.matilde.lam01.util.recorder.AndroidAudioRecorder
import java.io.File
import java.io.IOException
import kotlin.math.log10

@Composable
fun AudioRecordingDialog(onDismiss: () -> Unit, userLocation: LatLng, audioViewModel: AudioViewModel) {
    val context = LocalContext.current
    var audioFile: File?=null
    // Stati per la registrazione
    var isRecording by remember { mutableStateOf(false) }
    var decibelLevel by remember { mutableStateOf(0) }
    val recorder by lazy{
        AndroidAudioRecorder(context)
    }

    val player by lazy{
        AndroidAudioPlayer(context)
    }



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





    // Logica per aggiornare il livello del suono
    LaunchedEffect(isRecording) {
        if (isRecording) {
            File(context.cacheDir, "audio_temp.mp3").also {
                recorder.startRecording(it)
                audioFile = it
            }
        } else{
            recorder.stop()
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
                        try {
                            isRecording = true
                            Log.d("AudioRecordingDialog", "Inizio registrazione")
                        } catch (e: IOException) {
                            Toast.makeText(context, "Errore nella creazione del file audio", Toast.LENGTH_SHORT).show()
                            Log.e("AudioRecordingDialog", "Errore nella creazione del file: ${e.message}")
                        } catch (e: Exception){
                            Toast.makeText(context, "Errore generico: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("AudioRecordingDialog", "Errore generico: ${e.message}")
                        }
                    } else {
                        recorder.stop()
                        isRecording = false
                    }
                }) {
                    Text(if (isRecording) "Ferma Registrazione" else "Inizia Registrazione")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (isRecording) {
                        recorder.stop()
                        isRecording = false
                    }
                    try {
                        player.playFile(audioFile?:return@Button)
                        Log.d("AudioRecordingDialog", "Riproduzione del file: ${audioFile?.absolutePath}")
                    } catch (e: IOException) {
                        Toast.makeText(context, "Errore durante la riproduzione del file audio", Toast.LENGTH_SHORT).show()
                        Log.e("AudioRecordingDialog", "Errore nel MediaPlayer: ${e.message}")
                    } finally {
                        player.stop()
                    }
                }) {
                    Text("Riascolta")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            audioFile.let { file ->
                Button(onClick = {
                    audioViewModel.clearUploadStatus()
                    userLocation?.let { location ->
                        if (file != null) {
                            audioViewModel.uploadAudio(
                                file = file,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }
                        Log.d("AudioViewModel", "Caricamento dell'audio: ${file?.name}")

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

