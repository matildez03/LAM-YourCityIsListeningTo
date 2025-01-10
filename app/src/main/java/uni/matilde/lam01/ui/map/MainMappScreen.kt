package uni.matilde.lam01.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import uni.matilde.lam01.data.local.PreferencesHelper
import uni.matilde.lam01.data.remote.models.AudioResponse
import uni.matilde.lam01.ui.DrawerContent
import uni.matilde.lam01.ui.audio.AudioInfoBottomSheet
import uni.matilde.lam01.ui.audio.AudioRecordingDialog
import uni.matilde.lam01.ui.audio.AudioViewModel
import uni.matilde.lam01.ui.auth.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview


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
    val errorMessage by viewModel.errorMessage.observeAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    // Ottenere il nome della posizione
    val locationName by viewModel.locationName.observeAsState("Posizione sconosciuta")
    val audioResponse by viewModel.audio.observeAsState() // Osserva il risultato di getAudio
    val coroutineScope = rememberCoroutineScope()
    var selectedAudio by remember { mutableStateOf<AudioResponse?>(null) }
    var selectedLocationName by remember { mutableStateOf<String?>(null) }
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    var filterText by remember { mutableStateOf("") }


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
    val markers by viewModel.filteredMarkers.observeAsState()

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
                viewModel.fetchMarkersForUserLocation(
                    position, 1000.0
                ) // Filtra i marker entro 1 km
                Log.d("MainMapScreen", "Markers generati in base alla posizione dell'utente")
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Errore durante la geocodifica: ${e.message}")
        }
    }

    LaunchedEffect(errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(cameraPositionState.position) {
        snapshotFlow { cameraPositionState.position }.distinctUntilChanged()
            .debounce(300) // Evita aggiornamenti troppo frequenti
            .collect { cameraPosition ->
                viewModel.fetchMarkersForVisibleArea(
                    center = cameraPosition.target, // Centro della mappa
                    zoomLevel = cameraPosition.zoom // Livello di zoom
                )
            }
        Log.d("MainMapScreen", "Markers generati in base alla posizione della videocamera")

    }


    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)


    ModalNavigationDrawer(drawerState = drawerState,
        gesturesEnabled = false, // Disabilita l'apertura tramite gesture
        drawerContent = {
            DrawerContent(
                navController, onClose = {
                    scope.launch { drawerState.close() }
                }, authViewModel = authViewModel
            )
        }) {
        ModalBottomSheetLayout(sheetState = bottomSheetState, sheetContent = {
            selectedAudio?.let { audio ->
                AudioInfoBottomSheet(
                    audio = audio,
                    locationName = selectedLocationName ?: "Posizione sconosciuta"
                )
            }
        }) {

            Scaffold(topBar = @androidx.compose.runtime.Composable {
                TopAppBar(title = { Text("Mappa") }, navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                        Log.d("click event", "Button di apertura menù cliccato")
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Apri Menù")
                    }
                })
            }) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues) // Applica il padding fornito da Scaffold
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        //barra di ricerca
                        TextField(
                            value = filterText,
                            onValueChange = { text -> filterText = text },
                            label = { Text("Filtra") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search, contentDescription = "Cerca"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color(0xFFF0F0F0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        Button(
                            onClick = { viewModel.fetchFilteredMarkers(filterText) },
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Avvia ricerca")
                        }

                        // Pulsante "Rimuovi filtro"
                        Button(
                            onClick = {
                                filterText = "" // Resetta il campo di testo
                                userLocation?.let {
                                    viewModel.fetchMarkersForUserLocation(
                                        it, 1000.0
                                    ) // Filtra i marker entro 1 km
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rimuovi filtro")
                        }
                    }
                    Log.d("MainMapScreen", "Prima di GoogleMap")
                    GoogleMap(
                        modifier = Modifier.weight(1f),
                        cameraPositionState = cameraPositionState,
                        uiSettings = mapUiSettings,
                        properties = mapProperties
                    ) {
                        Log.d("MainMapScreen", "Dentro GoogleMap")

                        if (markers.isNullOrEmpty()) {
                            Log.d("MainMapScreen", "Nessun marker disponibile")
                            Toast.makeText(context,"Nessun marker disponibile", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("MainMapScreen", "Inizio caricamento markers")
                            // Aggiunge marker sulla mappa
                            markers?.forEach { marker ->
                                //TODO:  rimuovere dal main thread?
                                Log.d("GoogleMapDebug", "Marker posizione: ${marker.position}")
                                if (marker.position.latitude.isNaN() || marker.position.longitude.isNaN()) {
                                    Log.e("MainMapScreen", "Marker non valido: ${marker.audioId}")
                                } else {
                                    Marker(state = MarkerState(position = marker.position),
                                        //title = "Posizione marker",
                                        snippet = "Dettagli registrazione", onClick = {
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
                                                Log.d(
                                                    "Marker", "Marker cliccato: ${marker.position}"
                                                )
                                            }
                                            false // Ritorna false per mantenere il comportamento predefinito del marker
                                        })
                                }
                            }
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
                        AudioRecordingDialog(onDismiss = {
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
                    AlertDialog(onDismissRequest = { showUploadResultDialog = false },
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
                        })
                }
            }
        }
    }
}

fun onMarkerClick(audioInfo: AudioResponse, positionName: String) {

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


