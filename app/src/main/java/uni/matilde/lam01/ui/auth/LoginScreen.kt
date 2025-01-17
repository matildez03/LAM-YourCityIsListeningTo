package uni.matilde.lam01.ui.auth

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    navigateToSignUp: () -> Unit,
    navigateToHome: () -> Unit,
    factory: AuthViewModelFactory
) {
    val viewModel: AuthViewModel = viewModel(factory = factory)

    // Variabili per i campi di input
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Osserva lo stato del ViewModel
    val loginState by viewModel.authState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
