package uni.matilde.lam01.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.w3c.dom.Text
import uni.matilde.lam01.ui.auth.AuthViewModel


@Composable
fun DrawerContent(navController: NavController, onClose: () -> Unit, authViewModel: AuthViewModel) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Chiudi menu")
                Log.d("click event","Button di apertura menù cliccato")

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Menu",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        HorizontalDivider()

        DrawerItem("Map", Icons.Default.Home, onClick = {
            navController.navigate("map")
            onClose()
        })

        /*
        DrawerItem("Profile", Icons.Default.Person, onClick = {
            navController.navigate("profile")
            onClose()
        })

         */


        DrawerItem("Le mie registrazioni", Icons.Default.Favorite, onClick = {
            navController.navigate("audios")
            onClose()
        })

        DrawerItem("Notifiche", Icons.Default.Email, onClick = {
            navController.navigate("notifications")
            onClose()
        })
        Spacer(modifier = Modifier.weight(1f)) // Spinge il bottone di Logout in basso


        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colorScheme.error, // Colore di sfondo
                contentColor = MaterialTheme.colorScheme.onError  // Colore del testo e delle icone
            )
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.padding(end = 8.dp))
            Text("Logout")
        }

        // Pulsante di Eliminazione Account
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(Icons.Default.Person, contentDescription = "Elimina Account", modifier = Modifier.padding(end = 8.dp))
            Text("Elimina Account")
        }
    }
    // Dialogo di conferma Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false }, // Chiudi il dialogo se l'utente clicca fuori
            title = { Text("Conferma Logout") },
            text = { Text("Sei sicuro di voler effettuare il logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onClose() // Chiudi il drawer
                    navController.navigate("auth") {
                        popUpTo("map") { inclusive = true } // Ripulisce lo stack di navigazione
                    }
                }) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialogo di conferma Eliminazione Account
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Conferma Eliminazione Account") },
            text = { Text("Questa azione è irreversibile. Sei sicuro di voler eliminare il tuo account?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    Log.d("Delete","click avvenuto sul tasto di eliminazione")
                    authViewModel.deleteAccount() // Azione di eliminazione account
                }) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun DrawerItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

