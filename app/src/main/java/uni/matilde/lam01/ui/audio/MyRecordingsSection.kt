package uni.matilde.lam01.ui.audio

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UserRecordingsScreen(
    viewModel: MyRecordingsViewModel,
    onBack: () -> Unit
) {
    val recordings by viewModel.recordings.observeAsState(initial = emptyList())
    val context = LocalContext.current

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

        if (recordings.isEmpty()) {
            Text(
                text = "Nessuna registrazione disponibile",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(recordings) { recording ->
                    RecordingItem(
                        recording = recording,
                        onToggleVisibility = {
                            if (recording.isHidden) {
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
}

@Composable
fun RecordingItem(
    recording: MyAudiosResponse,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp)
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = recording.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Caricato il: ${recording.uploadDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onToggleVisibility,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recording.isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (recording.isHidden) "Mostra" else "Nascondi")
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

@Preview(showBackground = true)
@Composable
fun UserRecordingsScreenPreview() {
    val fakeRecordings = listOf(
        MyAudiosResponse(id = 1, title = "Registrazione 1", uploadDate = "2025-01-01", isHidden = false),
        MyAudiosResponse(id = 2, title = "Registrazione 2", uploadDate = "2025-01-02", isHidden = true)
    )

    val fakeViewModel = object : MyRecordingsViewModel(AudioRepository()) {
        init {
            _recordings.value = fakeRecordings
        }
    }

    UserRecordingsScreen(viewModel = fakeViewModel, onBack = {})
}
