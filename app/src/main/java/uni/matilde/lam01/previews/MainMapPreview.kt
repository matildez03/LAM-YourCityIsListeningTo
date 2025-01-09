/*package uni.matilde.lam01.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import uni.matilde.lam01.data.remote.repository.MapRepository
import uni.matilde.lam01.ui.map.MapViewModel
import uni.matilde.lam01.ui.map.MainMapScreen
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.repository.AudioRepository
import uni.matilde.lam01.data.remote.repository.AuthRepository

// Fake implementation di MapRepository
class FakeMapRepository : MapRepository {
    override fun getMarkers(): List<MarkerState> {
        return listOf(
            MarkerState(position = LatLng(44.4949, 11.3426)), // Marker su Bologna
            MarkerState(position = LatLng(45.4642, 9.1900))   // Marker su Milano
        )
    }
}

// Fake implementation di AuthRepository
class FakeAuthRepository : AuthRepository {
    // Aggiungi metodi necessari con dati fittizi
}

// Fake implementation di AudioRepository
class FakeAudioRepository : AudioRepository {
    // Aggiungi metodi necessari con dati fittizi
}

@Preview(
    name = "Preview MainMapScreen",
    showBackground = true
)
@Composable
fun MainMapPreview() {
    // Crea un'istanza di Context per PreferencesHelper
    val context = LocalContext.current

    // Usa i Fake Repository
    val fakeMapRepository = FakeMapRepository()
    val fakeAuthRepository = FakeAuthRepository()
    val fakeAudioRepository = FakeAudioRepository()

    // Crea i ViewModel con i Fake Repository
    val exampleViewModel = MapViewModel(mapRepository = fakeMapRepository)

    // Simula un NavController
    val exampleNavController = NavController(context)

    // Crea il PreferencesHelper
    val examplePreferencesHelper = PreferencesHelper(context)

    // Richiama il tuo composable principale per la preview
    MainMapScreen(
        viewModel = exampleViewModel,
        authViewModel = fakeAuthRepository, // Usa i Fake Repository
        audioViewModel = fakeAudioRepository,
        navController = exampleNavController,
        preferencesHelper = examplePreferencesHelper
    )
}
*/