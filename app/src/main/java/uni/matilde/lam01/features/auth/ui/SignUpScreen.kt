package uni.matilde.lam01.features.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SignUpScreen(
    navigateToLogin: () -> Unit,
    factory: AuthViewModelFactory
) {
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

        Image(
            painter = painterResource(id = R.drawable.soundmap_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

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
            is AuthState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            is AuthState.Success<*> -> {
                Text(
                    text = "Registrazione avvenuta con successo!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
                // Resetta lo stato per evitare comportamenti non desiderati al rientro
                LaunchedEffect(Unit) {
                    viewModel.resetAuthState()
                    navigateToLogin()
                }
            }
            is AuthState.Error -> {
                Text(
                    text = state.message ?: "Errore sconosciuto durante la registrazione",
                    modifier = Modifier.padding(8.dp)
                )
            }
            else -> {
            }
        }
    }
}
