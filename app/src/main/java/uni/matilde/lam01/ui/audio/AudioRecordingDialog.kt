package uni.matilde.lam01.ui.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
    val username = preferencesHelper.getUsername() ?: "utente_anonimo"

    var latitude = ""
    var longitude = ""
    // Stati per la registrazione
    val isRecording by audioViewModel.isRecording.collectAsState()
    var decibelLevel by remember { mutableStateOf(0) }

    // Stati per i permessi
    val hasRecordAudioPermission by audioViewModel.hasAudiorecordPermission.collectAsState()
    val hasWriteExPermission by audioViewModel.hasWriteExPermission.collectAsState()
    val hasReadExPermission by audioViewModel.hasReadExPermission.collectAsState()

    val currentRecordingPath by audioViewModel.currentRecordingPath.collectAsState()
    val mp3AudioPath by audioViewModel.mp3AudioPath.collectAsState()
    val errorMessage by audioViewModel.errorMessage.observeAsState()

    // Launcher per il permesso di registrazione audio
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioViewModel.checkRecordAudioPermission(context)
    }

    // Launcher per il permesso di scrittura
    val writeExLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioViewModel.checkWriteExternalStoragePermission(context)
    }

    // Launcher per il permesso di lettura
    val readExLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioViewModel.checkReadExternalStoragePermission(context)
    }

    // Verifica e richiedi permessi mancanti all'avvio
    LaunchedEffect(Unit) {
        audioViewModel.checkRecordAudioPermission(context)
        audioViewModel.checkWriteExternalStoragePermission(context)
        audioViewModel.checkReadExternalStoragePermission(context)

        if (!hasRecordAudioPermission) {
            recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        if (!hasWriteExPermission && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.Q) {
            writeExLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!hasReadExPermission) {
            readExLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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
                    // Button di start
                    if (!isRecording) {
                        if (!hasRecordAudioPermission || !hasWriteExPermission) {
                            Toast.makeText(
                                context,
                                "Concedi i permessi di registrazione e l'accesso ai file per procedere!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val success =
                                audioViewModel.startRecording(context, userLocation, username)
                            if (!success) {
                                Toast.makeText(
                                    context,
                                    "Errore durante l'avvio della registrazione",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Registrazione avviata", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        // Button di interruzione
                        val mp3FilePath = audioViewModel.stopRecording(context)
                        if (mp3FilePath == null) {
                            Toast.makeText(
                                context,
                                "Errore durante l'interruzione della registrazione",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Registrazione completata",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }) {
                    Text(if (isRecording) "Interrompi Registrazione" else "Inizia Registrazione")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RIASCOLTA
                Button(onClick = {
                    if(!hasReadExPermission){
                        Toast.makeText(context, "Concedi i permessi di lettura file per procedere!", Toast.LENGTH_SHORT).show()
                    } else {
                        mp3AudioPath?.let { path ->
                            audioViewModel.playRecording(context, path)
                        } ?: Toast.makeText(context, "Nessun file disponibile per la riproduzione", Toast.LENGTH_SHORT).show()
                    }
                },
                    enabled = (mp3AudioPath!=null)
                ) {
                    Text("Riascolta")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                /*
                TODO: IMPLEMENTA
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

                 */
            },
                enabled = !isRecording
            ) {
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


