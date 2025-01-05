package uni.matilde.lam01.ui.audio

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import uni.matilde.lam01.data.remote.models.AudioResponse


@Composable
fun AudioInfoBottomSheet(audio: AudioResponse, locationName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Dettagli Registrazione", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Autore: ${audio.creator_username}")
        Text(text= "Posizione: ${locationName}")
        Text(text = "Coordinate: ${audio.latitude}, ${audio.longitude}")
        Text(text = "Genere: ${audio.tags.genre?.maxByOrNull{it.value}?.key ?: "Non disponibile"}")
        Text(text = "Mood: ${audio.tags.mood?.maxByOrNull{it.value}?.key ?: "Non disponibile"}")
        Text(text = "Bpm: ${audio.tags.bpm ?: "Non disponibile"}")
        Text(text = "Danceability: ${audio.tags.danceability ?: "Non disponibile"}")


        Spacer(modifier = Modifier.height(16.dp))

    }
}
