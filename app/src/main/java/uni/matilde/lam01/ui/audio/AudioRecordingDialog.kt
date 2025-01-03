package uni.matilde.lam01.ui.audio

import android.Manifest
import android.content.Context
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
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.util.player.AndroidAudioPlayer
import uni.matilde.lam01.util.recorder.AndroidAudioRecorder
import uni.matilde.lam01.util.recorder.AudioRecorder
import java.io.File
import java.io.IOException
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

@Composable
fun AudioRecordingDialog(
    onDismiss: () -> Unit,
    userLocation: LatLng,
    audioViewModel: AudioViewModel,
    preferencesHelper: PreferencesHelper
) {
    val context = LocalContext.current
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
            //mostra dettagli decibel ecc
        }
        /*
        INTERRUZIONE REGISTRAZIONE
         */
        else {
            //Log.d("AudioRecordingDialog", "Prima di interrompere: Esiste: ${audioFile?.exists()}, Lunghezza: ${audioFile?.length()}")

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

                //INIZIA REGITRAZIONE
                Button(onClick = {
                    if (!hasAudioPermission) {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        return@Button
                    }
                    val username = preferencesHelper.getUsername()
                    mp4FilePath =
                        context.filesDir.absolutePath + "/" + username + "_" + userLocation + ".mp4"
                    startRecording(context, recorder, mp4FilePath)
                    isRecording = true

                }) {
                    Text("Inizia Registrazione")
                }

                //INTERROMPI REGISTRAZIONE
                Button(onClick = {
                    if (!hasAudioPermission) {
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        return@Button
                    }
                    val username = preferencesHelper.getUsername()
                    // Convete il file mp4 in mp3
                    mp3FilePath =
                        context.filesDir.absolutePath + "/" + username + "_" + userLocation + ".mp3"
                    stopRecording(context, recorder, mp4FilePath, mp3FilePath)

                }) {
                    Text("Ferma Registrazione")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RIASCOLTA
                Button(onClick = {
                    playRecording(context, mp3FilePath, player)
                }) {
                    Text("Riascolta")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                val file = File(mp3FilePath)
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
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )

}

fun startRecording(context: Context, recorder: AudioRecorder, mp4FilePath: String) {
    Log.d("AudioRecordingDialog", "Percorso della cache: ${context.externalCacheDir?.absolutePath}")

    // Assicuriamoci che la directory della cache esista e sia scrivibile
    val cacheDir = context.externalCacheDir

    if (cacheDir?.exists() == false) {
        val isDirCreated = context.cacheDir.mkdirs()
        if (!isDirCreated) {
            Log.e("AudioRecordingDialog", "Impossibile creare la directory della cache")
            Toast.makeText(
                context,
                "Errore nella creazione della directory della cache",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    if (cacheDir?.canWrite() == true) {
        Log.e("AudioRecordingDialog", "La directory cache non è scrivibile.")
    }

    try {
        val mp4File = File(mp4FilePath)
        Log.d(
            "AudioRecordingDialog",
            "File creato: ${mp4File.absolutePath}, Esiste: ${mp4File.exists()}, Scrivibile: ${mp4File.canWrite()}"
        )
        recorder.startRecording(mp4File)
    } catch (e: Exception) {
        Toast.makeText(context, "Errore nella registrazione: ${e.message}", Toast.LENGTH_LONG)
            .show()
        Log.e("AudioRecordingDialog", "Errore nella registrazione: ${e.message}")
    }
}

fun stopRecording(
    context: Context,
    recorder: AudioRecorder,
    mp4FilePath: String,
    mp3FilePath: String
) {

    try {
        recorder.stop()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val ffmpegCommand = "-i $mp4FilePath -codec:a libmp3lame -qscale:a 2 $mp3FilePath"


    FFmpegKit.executeAsync(ffmpegCommand) { session ->
        val returnCode = session.returnCode
        if (ReturnCode.isSuccess(returnCode)) {
            Log.d("FFmpegKit", "Conversion to MP3 succeeded.")

            val mp4File = File(mp4FilePath)
            if (mp4File.exists()) {
                val deleted = mp4File.delete()
                if (deleted) {
                    Log.d("File Deletion", "MP4 file deleted successfully.")
                } else {
                    Log.d("File Deletion", "Failed to delete MP4 file.")
                }
            }
        } else if (ReturnCode.isCancel(returnCode)) {
            Log.d("FFmpegKit", "Conversion to MP3 was canceled by the user.")
        } else {
            val failStackTrace = session.failStackTrace ?: "Unknown error"
            Log.e(
                "FFmpegKit",
                "Conversion to MP3 failed. Return code: $returnCode. Details: $failStackTrace"
            )

            Toast.makeText(context, "C'è stato un errore!", Toast.LENGTH_SHORT).show()
        }
    }

}

fun playRecording(context: Context, mp3FilePath: String, player: AndroidAudioPlayer) {
    val audioFile = File(mp3FilePath)

    if (audioFile.exists() && audioFile.length() > 0) {
        if (audioFile.length() == 0L) {
            throw IOException("Il file audio è vuoto: ${audioFile.absolutePath}")
        }
        try {
            player.playFile(audioFile)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Errore durante la riproduzione",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("AudioRecordingDialog", "Errore riproduzione: ${e.message}")
        }
        //eccezioni e finally gestito all'interno della classe AndroidAudioPlayer.kt
    } else {
        Log.e("AudioRecordingDialog", "File audio vuoto o inesistente")

        Toast.makeText(context, "Nessun file disponibile", Toast.LENGTH_SHORT)
            .show()
    }

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

