package uni.matilde.lam01.ui.audio

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import uni.matilde.lam01.data.local.PreferencesHelper
import java.io.File


@Composable
fun AudioRecordingDialog(
    onDismiss: () -> Unit,
    onUploadSuccess: () -> Unit,
    userLocation: LatLng,
    audioViewModel: AudioViewModel,
    preferencesHelper: PreferencesHelper
) {
    val context = LocalContext.current
    val username = preferencesHelper.getUsername() ?: "utente_anonimo"

    // Stati per la registrazione
    val isRecording by audioViewModel.isRecording.collectAsState()
    var decibelLevel by remember { mutableStateOf(0) }

    // Stati per i permessi
    val hasRecordAudioPermission by audioViewModel.hasAudiorecordPermission.collectAsState()
    val hasWriteExPermission by audioViewModel.hasWriteExPermission.collectAsState()
    val hasReadExPermission by audioViewModel.hasReadExPermission.collectAsState()

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

    var recordingDuration by remember { mutableStateOf(0) }

    // Timer durante la registrazione
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0 // Resetta il timer
            while (isRecording) {
                delay(1000L)
                recordingDuration++
            }
        }
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


    val uploadStatus by audioViewModel.uploadStatus.observeAsState()
    LaunchedEffect(uploadStatus) {
        uploadStatus?.let { success ->
            val message = if (success) {
                "Caricamento completato con successo!"
            } else {
                "Errore durante il caricamento dell'audio."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (success) {
                onDismiss() // Chiudi il dialogo di registrazione
                onUploadSuccess() // Mostra il dialogo di successo del caricamento
            } else {
                Toast.makeText(context, "Errore durante il caricamento dell'audio.", Toast.LENGTH_SHORT).show()
                audioViewModel.clearUploadStatus()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registra e Riascolta") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

                // Mostra la durata della registrazione
                if (isRecording) {
                    Text(
                        text = "Durata: ${recordingDuration}s",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Animazione di recording
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Cerchi animati
                        repeat(3) { index ->
                            val animationProgress by rememberInfiniteTransition()
                                .animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    )
                                )
                            Canvas(modifier = Modifier.size(100.dp)) {
                                drawCircle(
                                    color = Color.Blue.copy(alpha = 0.3f),
                                    radius = size.minDimension / 2 * animationProgress
                                )
                            }
                        }
                        // Punto centrale
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(color = Color.Blue, radius = size.minDimension / 2)
                        }
                    }
                }

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
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )) {
                    Text(if (isRecording) "Interrompi Registrazione" else{ if(mp3AudioPath==null) "Inizia Registrazione" else "Riprova"})
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mp3AudioPath!=null) {
                    Text(
                        text = "Durata: ${recordingDuration}s",
                        modifier = Modifier.padding(8.dp)
                    )
                }

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
                    enabled = (mp3AudioPath!=null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Riascolta")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                if(mp3AudioPath!=null) {
                    audioViewModel.clearUploadStatus()
                    audioViewModel.uploadAudio(
                        username,
                        mp3AudioPath!!,
                        userLocation.latitude,
                        userLocation.longitude
                    )
                    Log.d("AudioViewModel", "Caricamento dell'audio: ${mp3AudioPath}")
                }
            } ,
                enabled = (!isRecording && mp3AudioPath!=null),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Conferma e Invia")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
            }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            )) {
                Text("Annulla")
            }
        }
    )

}


