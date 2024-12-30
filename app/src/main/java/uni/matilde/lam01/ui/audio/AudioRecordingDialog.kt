package uni.matilde.lam01.ui.audio

import android.media.AudioRecord
import android.media.AudioFormat
import android.media.MediaRecorder
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.delay
import kotlin.math.log10

@Composable
fun AudioRecordingDialog(onDismiss: () -> Unit) {

    val context = LocalContext.current

    // Stato per sapere se l'utente ha concesso il permesso
    var hasAudioPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher per richiedere il permesso
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasAudioPermission = granted
            if (!granted) {
                Toast.makeText(context, "Permesso per registrare audio negato", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Stati per la registrazione
    var isRecording by remember { mutableStateOf(false) }
    var decibelLevel by remember { mutableStateOf(0) }

    // Configura AudioRecord
    val audioBuffer = ShortArray(1024)
    val recorder = remember {
        try{
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioBuffer.size * 2
        )
        } catch (e: Exception) {
        Log.e("AudioRecordingDialog", "Errore nella configurazione di AudioRecord: ${e.message}")
        null
    }
    }

    // Logica per aggiornare il livello del suono
    LaunchedEffect(isRecording) {
        if (isRecording && recorder != null) {
            recorder.startRecording()
            while (isRecording) {
                val result = recorder.read(audioBuffer, 0, audioBuffer.size)
                if (result < 0) {
                    Log.e("AudioRecordingDialog", "Errore durante la lettura dell'audio: $result")
                    break
                }
                val amplitude = audioBuffer.maxOrNull()?.toInt() ?: 0
                decibelLevel = if (amplitude > 0) (20 * log10(amplitude.toDouble())).toInt() else 0
                delay(100)
            }
            recorder.stop()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrazione Audio") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasAudioPermission) {
                    // Mostra il livello del suono solo se il permesso è stato concesso
                    Text("Livello suono: ${decibelLevel}dB")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        isRecording = !isRecording
                    }) {
                        Text(if (isRecording) "Ferma Registrazione" else "Avvia Registrazione")
                    }
                } else {
                    // Richiede all'utente di concedere il permesso
                    Text("È necessario il permesso per registrare audio.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }) {
                        Text("Concedi Permesso")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

