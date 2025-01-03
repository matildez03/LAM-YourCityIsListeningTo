package uni.matilde.lam01.ui.audio

import android.media.AudioRecord
import android.media.AudioFormat
import android.media.MediaRecorder
import android.Manifest
import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uni.matilde.lam01.util.player.AndroidAudioPlayer
import uni.matilde.lam01.util.recorder.AndroidAudioRecorder
import java.io.File
import java.io.IOException
import kotlin.math.log10

@Composable
fun AudioRecordingDialog(
    onDismiss: () -> Unit,
    userLocation: LatLng,
    audioViewModel: AudioViewModel
) {
    val context = LocalContext.current
    var audioFile by remember { mutableStateOf<File?>(null) }
    var mp4FilePath = ""
    var mp3FilePath = ""
    // Stati per la registrazione
    var isRecording by remember { mutableStateOf(false) }
    var decibelLevel by remember { mutableStateOf(0) }
    val recorder by lazy {
        AndroidAudioRecorder(context)
    }

    val player by lazy {
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
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val writeGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        val readGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        val recordGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        if (!writeGranted) {
            Log.e("AudioRecordingDialog", "Permesso per scrivere nella cache negato.")
        }

        if (!readGranted) {
            Log.e("AudioRecordingDialog", "Permesso per scrivere nella cache negato.")
        }

        if (!recordGranted) {
            Log.e("AudioRecordingDialog", "Permesso per registrare audio negato.")
        }

        if (writeGranted && recordGranted) {
            Log.d("AudioRecordingDialog", "Tutti i permessi concessi.")
        }
    }


    val hasWritePermission = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasWritePermission) {
        Log.e("AudioRecordingDialog", "Permessi mancanti per scrivere nella cache.")
    }

    val hasReadPermission = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasReadPermission) {
        Log.e("AudioRecordingDialog", "Permessi mancanti per leggere/scrivere.")
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        )

    }


    // Logica per aggiornare il livello del suono
    LaunchedEffect(isRecording) {
        if (isRecording) {
            // Log del percorso della cache
            Log.d("AudioRecordingDialog", "Percorso della cache: ${context.cacheDir.absolutePath}")


            // Assicuriamoci che la directory della cache esista
            val cacheDir = context.cacheDir
            if (!cacheDir.canWrite()) {
                Log.e("AudioRecordingDialog", "La directory cache non è scrivibile.")
            }

            if (!context.cacheDir.exists()) {
                val isDirCreated = context.cacheDir.mkdirs()
                if (!isDirCreated) {
                    Log.e("AudioRecordingDialog", "Impossibile creare la directory della cache")
                    Toast.makeText(
                        context,
                        "Errore nella creazione della directory della cache",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@LaunchedEffect
                }
            }


            //clearCache(context)
            audioFile = File(context.cacheDir, "audio_temp.mp4")
            try {
                withContext(Dispatchers.IO) {
                    if (!audioFile!!.exists()) {
                        audioFile!!.createNewFile()
                    }
                    recorder.startRecording(audioFile!!)
                }

                Log.d(
                    "AudioRecordingDialog",
                    "File creato: ${audioFile?.absolutePath}, Esiste: ${audioFile?.exists()}, Scrivibile: ${audioFile?.canWrite()}"
                )

                Log.d(
                    "AudioRecordingDialog",
                    "Inizio registrazione su: ${audioFile?.absolutePath}"
                )

            } catch (e: IOException) {
                Log.e("AudioRecordingDialog", "Errore nella creazione del file: ${e.message}")
                Toast.makeText(context, "Errore nella creazione del file audio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        /*
        INTERRUZIONE REGISTRAZIONE
         */
        else {
            //Log.d("AudioRecordingDialog", "Prima di interrompere: Esiste: ${audioFile?.exists()}, Lunghezza: ${audioFile?.length()}")
            try {
                withContext(Dispatchers.IO) {
                    recorder.stop()
                    delay(500) // Aspetta mezzo secondo per garantire la scrittura
                }
                Log.d(
                    "AudioRecordingDialog",
                    "Fine registrazione. Esiste: ${audioFile?.exists()}, Lunghezza: ${audioFile?.length()}"
                )
            } catch (e: IOException) {
                Log.e(
                    "AudioRecordingDialog",
                    "Errore durante l'interruzione della registrazione: ${e.message}"
                )
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
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        return@Button
                    }
                    isRecording = !isRecording
                }) {
                    Text(if (isRecording) "Ferma Registrazione" else "Inizia Registrazione")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {

                    if (isRecording == true) {
                        isRecording = false
                        Log.d(
                            "AudioRecordingDialog",
                            "Interruzione registrazione. Esiste: ${audioFile?.exists()}, Lunghezza: ${audioFile?.length()}"
                        )
                    }

                    if (audioFile != null && audioFile!!.exists() && audioFile!!.length() > 0) {
                        if (audioFile?.length() == 0L) {
                            throw IOException("Il file audio è vuoto: ${audioFile?.absolutePath}")
                        }
                        try {
                            player.playFile(audioFile!!)
                        } catch (e: IOException) {
                            Toast.makeText(
                                context,
                                "Errore durante la riproduzione",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("AudioRecordingDialog", "Errore riproduzione: ${e.message}")
                        } catch (e: IllegalStateException) {
                            Toast.makeText(
                                context,
                                "Errore durante l'inizializzazione del player",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "AudioRecordingDialog",
                                "Errore stato del MediaPlayer: ${e.message}"
                            )
                        } finally {
                            player.stop()
                        }
                    } else {
                        Log.e("AudioRecordingDialog", "File audio vuoto o inesistente")

                        Toast.makeText(context, "Nessun file disponibile", Toast.LENGTH_SHORT)
                            .show()
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
                        Toast.makeText(
                            context,
                            "Posizione non disponibile, impossibile caricare l'audio",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text("Conferma e Invia")
                }
            } ?: run {
                Toast.makeText(
                    context,
                    "Nessun file disponibile per il caricamento",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

fun clearCache(context: Context) {
    val cacheDir = context.cacheDir
    if (cacheDir.isDirectory) {
        val files = cacheDir.listFiles()
        files?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }
}

