package uni.matilde.lam01.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import uni.matilde.lam01.ui.DrawerContent


@Composable
fun MainMapScreen(viewModel: MapViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current

    // Gestione dei permessi di posizione
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.updateUserLocation(
                    LatLng(
                        44.4949,
                        11.3426
                    )
                ) // Posizione iniziale se il permesso è concesso
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.updateUserLocation(LatLng(44.4949, 11.3426)) // Posizione di default
        }
    }

    // Osserva i dati LiveData
    val userLocation by viewModel.userLocation.observeAsState()
    val markers by viewModel.markers.observeAsState(initial = emptyList())

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation ?: LatLng(44.4949, 11.3426), // Bologna come fallback
            12f
        )
    }

    // Aggiorna la posizione della fotocamera quando la posizione dell'utente cambia
    LaunchedEffect(userLocation) {
        val position = userLocation ?: LatLng(44.4949, 11.3426) // Fallback a Bologna
        cameraPositionState.position = CameraPosition.fromLatLngZoom(position, 12f)
    }


    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = rememberDrawerState(DrawerValue.Closed),
        drawerContent = {
            DrawerContent(navController, onClose = { scope.launch { drawerState.close() } }
            )
        }
    )
    {
        Scaffold(
            topBar = @androidx.compose.runtime.Composable {
                TopAppBar(
                    title = { Text("Mappa") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Person, contentDescription = "Apri Profilo")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = cameraPositionState
                ) {
                    // Aggiunge marker sulla mappa
                    markers.forEach { marker ->
                        MapMarker(
                            position = marker.position,
                            title = marker.title,
                            description = marker.description
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.fetchMarkers() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Aggiorna Marker")
                }
            }
        }
    }
}
