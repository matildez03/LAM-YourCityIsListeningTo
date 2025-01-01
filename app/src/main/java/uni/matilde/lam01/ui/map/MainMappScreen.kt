package uni.matilde.lam01.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.*
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.remote.repository.AuthRepository
import uni.matilde.lam01.ui.DrawerContent
import uni.matilde.lam01.ui.audio.AudioRecordingDialog
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.auth.AuthViewModel


@Composable
fun MainMapScreen(
    viewModel: MapViewModel = viewModel(),
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    navController: NavController
) {


    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapUiSettings = remember { MapUiSettings(myLocationButtonEnabled = true) }
    val mapProperties = remember { MapProperties(isMyLocationEnabled = true) }
    var showRecordingDialog by remember { mutableStateOf(false) } // Stato per il popup





    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) { //accede alla posizione dell'utente
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
                } else {
                    Log.w("MainMapScreen", "Posizione non disponibile. Usando il fallback.")
                    Toast.makeText(context, "Posizione non disponibile. Usando il fallback.", Toast.LENGTH_SHORT).show()
                    viewModel.updateUserLocation(LatLng(44.4949, 11.3426)) // Bologna
                }
            }
        } else {
            Log.w("MainMapScreen", "Permesso per localizzazione non abilitato.")
            Toast.makeText(context, "Abilita la localizzazione per migliorare l'esperienza.", Toast.LENGTH_LONG).show()
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
        userLocation?.let { position ->
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(position, 12f)
            )
        }
    }


    //aggiorna i markers in base allo zoom
    LaunchedEffect(cameraPositionState.position) {
        snapshotFlow { cameraPositionState.position.zoom }
            .distinctUntilChanged() // Aggiorna solo se il valore dello zoom cambia
            .debounce(300) // Riduce gli aggiornamenti frequenti
            .collect { zoomLevel ->
                userLocation?.let { position ->
                    viewModel.fetchMarkersForUserLocationAndZoom(position, zoomLevel)
                }?: Log.w("MainMapScreen", "Posizione non disponibile.")
            }
    }


    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)




    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // Disabilita l'apertura tramite gesture
        drawerContent = {
            DrawerContent(
                navController,
                onClose = {
                    scope.launch { drawerState.close() }
                },
                authViewModel = authViewModel
            )
        }
    )
    {
        Scaffold(
            topBar = @androidx.compose.runtime.Composable {
                TopAppBar(
                    title = { Text("Mappa") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                            Log.d("click event", "Button di apertura menù cliccato")
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Apri Menù")
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
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    properties = mapProperties
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
                    onClick = { showRecordingDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Aggiungi una registrazione")
                }
            }
            // Popup di registrazione
            if (showRecordingDialog) {
                userLocation?.let { AudioRecordingDialog(onDismiss = {
                    showRecordingDialog = false
                    audioViewModel.clearUploadStatus() // Resetta lo stato dell'upload
                }, userLocation = it, audioViewModel = audioViewModel) }
            }
        }
    }
}
