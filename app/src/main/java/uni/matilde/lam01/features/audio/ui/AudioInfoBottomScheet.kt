package uni.matilde.lam01.features.audio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uni.matilde.lam01.data.remote.models.AudioResponse


@Composable
fun AudioInfoBottomSheet(audio: AudioResponse, locationName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp) // Padding interno
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Dettagli Registrazione", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Autore: ${audio.creator_username}")
            Text(text = "Posizione: ${locationName}")
            Text(text = "Coordinate: ${audio.latitude}, ${audio.longitude}")
            Text(text = "Genere: ${audio.tags.genre?.maxByOrNull { it.value }?.key ?: "Non disponibile"}")
            Text(text = "Mood: ${audio.tags.mood?.maxByOrNull { it.value }?.key ?: "Non disponibile"}")
            Text(text = "Bpm: ${audio.tags.bpm ?: "Non disponibile"}")
            Text(text = "Danceability: ${audio.tags.danceability ?: "Non disponibile"}")


            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
