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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import uni.matilde.lam01.data.remote.repository.AuthRepository

@Composable
fun SignUpScreen(
    navigateToLogin: () -> Unit,
    factory: AuthViewModelFactory // Passa la factory come parametro
) {
    // Usa la factory per creare il ViewModel
    val viewModel: AuthViewModel = viewModel(factory = factory)

    // Variabili per i campi di input
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Osserva lo stato del ViewModel
    val signUpState by viewModel.authState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign Up", style = MaterialTheme.typography.headlineMedium)

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

        // Bottone di registrazione
        Button(
            onClick = { viewModel.signUp(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotEmpty() && password.isNotEmpty()
        ) {
            Text(text = "Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigazione verso la schermata di login
        TextButton(onClick = navigateToLogin) {
            Text(text = "Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stato di registrazione
        when (val state = signUpState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Success<*> -> {
                Text(text = "Registrazione avvenuta con successo!", color = MaterialTheme.colorScheme.primary)
                // Resetta lo stato e torna al login
                viewModel.resetAuthState()
                navigateToLogin()
            }
            is AuthState.Error -> {
                Text(
                    text = state.message ?: "Errore sconosciuto",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                // Stato iniziale o fallback
                Text(
                    text = "Inserisci le tue credenziali per effettuare il login.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
