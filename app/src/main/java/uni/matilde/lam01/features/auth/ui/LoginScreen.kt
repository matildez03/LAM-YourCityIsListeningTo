package uni.matilde.lam01.features.auth.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uni.matilde.lam01.R
import uni.matilde.lam01.features.auth.viewmodel.AuthState
import uni.matilde.lam01.features.auth.viewmodel.AuthViewModel
import uni.matilde.lam01.features.auth.viewmodel.AuthViewModelFactory

@Composable
fun LoginScreen(
    navigateToSignUp: () -> Unit,
    navigateToHome: () -> Unit,
    factory: AuthViewModelFactory
) {
    val viewModel: AuthViewModel = viewModel(factory = factory)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.authState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.soundmap_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo di input per lo username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo di input per la password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottone di login
        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotEmpty() && password.isNotEmpty()
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigazione verso la schermata di registrazione
        TextButton(onClick = navigateToSignUp) {
            Text(text = "Don't have an account? Sign up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stato di autenticazione
        when (val state = loginState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success<*> -> {
                // Naviga verso la home in caso di successo
                navigateToHome()
            }
            is AuthState.Error -> {
                Text(
                    text = state.message ?: "Errore sconosciuto",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {

            }
        }
    }
}
