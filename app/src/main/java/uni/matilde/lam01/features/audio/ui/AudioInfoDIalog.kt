package uni.matilde.lam01.features.audio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uni.matilde.lam01.data.local.AudioEntity

@Composable
fun AudioInfoDialog(onDismiss: () -> Unit, audio: AudioEntity){

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Dettagli registrazione") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Rende la colonna scrollabile
                    .padding(16.dp)  // Aggiunge margini interni
            ) {

                Text("BPM: ${audio.bpm}")
                Text("Danceability: ${audio.danceability}")
                Text("Mood: ${audio.mood}")
                Text("Genere: ${audio.genre}")
                Text("Loudness: ${audio.loudness}")
                Text("Instrument: ${audio.instrument}")
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("OK")
            }
        })

}