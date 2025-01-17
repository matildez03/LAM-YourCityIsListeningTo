package uni.matilde.lam01.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    navController: NavController
) {
    val totalAudios by viewModel.totalAudios.observeAsState(Result.success(0))
    val averageBpm by viewModel.averageBpm.observeAsState(Result.success(0.0))
    val moodDistribution by viewModel.moodDistribution.observeAsState(Result.success(emptyList()))
    val genreDistribution by viewModel.genreDistribution.observeAsState(Result.success(emptyList()))

    LaunchedEffect(Unit) {
        viewModel.getTotalAudios()
        viewModel.getAverageBpm()
        viewModel.getGenreDistribution()
        viewModel.getMoodDistribution()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistiche",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(route = "menu") }) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Apri Menù",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistiche generali
            StatisticsCard(
                title = "Audio Totali",
                content = {
                    if (totalAudios.isSuccess) {
                        Text("${totalAudios.getOrNull()}")
                    } else {
                        Text("Errore: ${totalAudios.exceptionOrNull()?.message ?: "Sconosciuto"}")
                    }
                }
            )

            StatisticsCard(
                title = "Media BPM",
                content = {
                    if (averageBpm.isSuccess) {
                        Text("${"%.2f".format(averageBpm.getOrNull())}")
                    } else {
                        Text("Errore: ${averageBpm.exceptionOrNull()?.message ?: "Sconosciuto"}")
                    }
                }
            )

            // Distribuzioni
            StatisticsCard(
                title = "Distribuzione degli stati d'animo",
                content = {
                    if (moodDistribution.isSuccess) {
                        // Ottieni i dati e ordina per valore decrescente
                        val moods = moodDistribution.getOrNull()?.sortedByDescending { it.count }
                            ?: emptyList()
                        if (moods.isEmpty()) {
                            Text("Nessun dato disponibile")
                        } else {
                            moods.forEach { mood ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mood.mood, // Nome del mood in grassetto
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${mood.count}", // Valore del conteggio
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Errore: ${moodDistribution.exceptionOrNull()?.message ?: "Sconosciuto"}")
                    }
                }
            )


            StatisticsCard(
                title = "Distribuzione dei generi",
                content = {
                    if (genreDistribution.isSuccess) {
                        val genres = genreDistribution.getOrNull()?.sortedByDescending { it.count }
                            ?: emptyList()
                        if (genres.isEmpty()) {
                            Text("Nessun dato disponibile")
                        } else {
                            genres.forEach { genre ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = genre.genre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${genre.count}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Errore: ${genreDistribution.exceptionOrNull()?.message ?: "Sconosciuto"}")
                    }
                }
            )
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

