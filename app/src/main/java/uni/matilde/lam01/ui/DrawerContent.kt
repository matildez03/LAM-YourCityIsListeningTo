package uni.matilde.lam01.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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




@Composable
fun DrawerContent(navController: NavController, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Chiudi menu")
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
        DrawerItem("Logout", Icons.Default.ExitToApp, onClick = {
            navController.navigate("logout")
            onClose()
        })
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

