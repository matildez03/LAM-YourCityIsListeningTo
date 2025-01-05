package uni.matilde.lam01.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.data.remote.repository.AuthRepository
import uni.matilde.lam01.ui.DrawerContent
import uni.matilde.lam01.ui.audio.AudioInfoBottomSheet
import uni.matilde.lam01.ui.audio.AudioRecordingDialog
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.auth.AuthViewModel
import java.util.Locale


@Composable
fun MainMapScreen(
    viewModel: MapViewModel,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    navController: NavController,
    preferencesHelper: PreferencesHelper
) {


    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapUiSettings = remember { MapUiSettings(myLocationButtonEnabled = true) }
    val mapProperties = remember { MapProperties(isMyLocationEnabled = true) }
    var showRecordingDialog by remember { mutableStateOf(false) } // Stato per il popup
    var showUploadResultDialog by remember { mutableStateOf(false) }
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    // Ottenere il nome della posizione
    val locationName by viewModel.locationName.observeAsState("Posizione sconosciuta")
    val audioResponse by viewModel.audio.observeAsState() // Osserva il risultato di getAudio
    val coroutineScope = rememberCoroutineScope()
    var selectedAudio by remember { mutableStateOf<AudioResponse?>(null) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)


    // Launcher per richiedere i permessi
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Aggiorna lo stato del permesso nel ViewModel
        viewModel.checkLocationPermission(context)
    }

    // Controlla il permesso all'avvio del composable
    LaunchedEffect(Unit) {
        viewModel.checkLocationPermission(context)
        delay(500) //aggiornamento valore
        if (hasLocationPermission == false) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            enableLocation(fusedLocationClient, viewModel)
        }
        Log.d("MainMapScreen", "Permesso alla posizione: ${hasLocationPermission}")
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
        try {
            userLocation?.let { position ->
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(position, 12f)
                )
                //Assegna un nome alla posizione
                viewModel.fetchLocationName(context, position.latitude, position.longitude)
                viewModel.fetchMarkers()

            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Errore durante la geocodifica: ${e.message}")
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
                } ?: Log.w("MainMapScreen", "Posizione non disponibile.")
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
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetContent = {
                selectedAudio?.let { audio ->
                    AudioInfoBottomSheet(
                        audio = audio,
                        locationName = selectedLocationName ?: "Posizione sconosciuta"
                    )
                }
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
                            //TODO:  rimuovere dal main thread?
                            Marker(
                                state = MarkerState(position = marker.position),
                                //title = "Posizione marker",
                                snippet = "Dettagli registrazione",
                                onClick = {
                                    coroutineScope.launch {
                                        // Ottieni il nome della posizione in una coroutine
                                        val positionName = viewModel.getLocationName(
                                            context = context,
                                            latitude = marker.position.latitude,
                                            longitude = marker.position.longitude
                                        )

                                        viewModel.getAudioInfo(marker.audioId)
                                        audioResponse?.let { it1 ->
                                            onMarkerClick(it1, positionName)
                                            Log.d(
                                                "MainMapScreen",
                                                "informazioni ottenute: ${it.toString()}"
                                            )
                                        }

                                        if (audioResponse != null) {
                                            selectedAudio = audioResponse
                                            selectedLocationName = positionName
                                            bottomSheetState.show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Impossibile caricare le informaioni del brano.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        Log.d("Marker", "Marker cliccato: ${marker.position}")
                                    }
                                    false // Ritorna false per mantenere il comportamento predefinito del marker
                                }
                            )
                        }
                        Log.d("MainMapScreen", "Eventuali markers caricati") //debug
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Posizione attuale: $locationName",
                        modifier = Modifier.padding(all = 16.dp)
                    )
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
                    userLocation?.let {
                        AudioRecordingDialog(
                            onDismiss = {
                                showRecordingDialog = false
                                audioViewModel.clearStates()
                            },
                            onUploadSuccess = {
                                showUploadResultDialog = true
                            } // Mostra il dialogo del risultato
                            ,
                            userLocation = it,
                            locationName = locationName,
                            audioViewModel = audioViewModel,
                            preferencesHelper = preferencesHelper)
                    }
                }

                // Dialogo per il risultato del caricamento
                if (showUploadResultDialog) {
                    AlertDialog(
                        onDismissRequest = { showUploadResultDialog = false },
                        title = { Text("Caricamento completato!") },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()) // Rende la colonna scrollabile
                                    .padding(16.dp)  // Aggiunge margini interni
                            ) {
                                val lastAudio = audioViewModel.audios.value?.lastOrNull()
                                Text("Brano caricato con successo!")
                                Text("BPM: ${lastAudio?.bpm}")
                                Text("Danceability: ${lastAudio?.danceability}")
                                Text("Mood: ${lastAudio?.mood}")
                                Text("Genere: ${lastAudio?.genre}")
                                Text("Loudness: ${lastAudio?.loudness}")
                                Text("Instrument: ${lastAudio?.instrument}")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showUploadResultDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

fun onMarkerClick(audioInfo:AudioResponse, positionName: String) {

}


@SuppressLint("MissingPermission")
fun enableLocation(fusedLocationClient: FusedLocationProviderClient, viewModel: MapViewModel) {
    Log.d("MainMapScreen", "Accedendo alla posizione")
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
            Log.d("MainMapScreen", "user location aggiornata: ${location}")
        } else {
            Log.w("MainMapScreen", "Posizione non disponibile. Usando il fallback.")
            viewModel.updateUserLocation(LatLng(44.4949, 11.3426)) // Bologna come fallback
        }
    }
}



