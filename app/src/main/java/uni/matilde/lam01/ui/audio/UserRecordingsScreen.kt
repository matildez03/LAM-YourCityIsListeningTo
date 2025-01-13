package uni.matilde.lam01.ui.audio

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import uni.matilde.lam01.data.local.AudioEntity
import uni.matilde.lam01.data.remote.models.MyAudiosResponse
import kotlin.math.round


@Composable
fun UserRecordingsScreen(
    viewModel: UserRecordingsViewModel,
    onBack: () -> Unit
) {
    val recordings by viewModel.displayedRecordings.observeAsState(initial = emptyList())
    val viewMessage by viewModel.viewMessage.collectAsState()
    val audioInfo by viewModel.audioInfo.observeAsState()
    var showAudioInfo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getRecordings()
    }

    // Mostra un Toast ogni volta che il messaggio cambia
    LaunchedEffect(viewMessage) {
        if (viewMessage?.trim()?.isNullOrEmpty() == false) {
            Toast.makeText(context, viewMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage() // Resetta il messaggio
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        // Header con il pulsante Indietro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Torna indietro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Le mie registrazioni",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recordings?.isEmpty()!!) {
            Text(
                text = "Nessuna registrazione disponibile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            LazyColumn {
                items(recordings) { recording ->
                    RecordingItem(
                        recording = recording,
                        onClick = {
                            viewModel.showRecordingInfo(recording.id)
                            showAudioInfo = true
                        },
                        onToggleVisibility = {
                            if (recording.hidden) {
                                viewModel.showRecording(recording.id)
                            } else {
                                viewModel.hideRecording(recording.id)
                            }
                        },
                        onDelete = {
                            viewModel.deleteRecording(recording.id)
                        }
                    )
                }
            }
        }
    }


    if (showAudioInfo && audioInfo != null) {
        AudioInfoDialog(
            onDismiss = {
                showAudioInfo = false
                viewModel.resetAudioInfo()
            },
            audio = audioInfo!!
        )
    }

}

@Composable
fun RecordingItem(
    recording: DisplayedAudioInfo,
    onClick: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer: MediaPlayer? = remember { null }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp)
            .padding(vertical = 8.dp)
            .clickable { onClick() } // Aggiunto il modificatore clickable
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            var position = ""
            if (recording.locationName.trim().isNullOrEmpty()) {
                position = recording.latLng.toString()
            } else {
                position = recording.locationName
            }
            Text(
                text = position,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = recording.timestamp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            // Stop playback
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                        } else {
                            // Start playback
                            try {
                                if (mediaPlayer?.isPlaying == true) {
                                    isPlaying = false
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                }
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(recording.filePath)
                                    prepare()
                                    start()
                                }
                                isPlaying = true
                                mediaPlayer?.setOnCompletionListener {
                                    isPlaying = false
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Errore nella riproduzione: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(
                                    "UserRecordingsScreen",
                                    "Errore nella riproduzione: ${e.message}"
                                )
                                isPlaying = false
                                mediaPlayer?.release()
                                mediaPlayer = null
                            }
                        }
                    },
                    enabled = !recording.filePath.isNullOrEmpty()!! // Disabilita se filePath è invalido

                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausa" else "Riproduci"
                    )
                }

                Button(
                    onClick = onToggleVisibility,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recording.hidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (recording.hidden) "Mostra" else "Nascondi")
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Elimina")
                }
            }
        }
    }
}



