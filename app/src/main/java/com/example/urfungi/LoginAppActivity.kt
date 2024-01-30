package com.example.urfungi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.urfungi.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth


class LoginAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()

                LoginScreen(
                    navController = navController,
                    onLoginSuccess = {
                        // Navegar al MainActivity con el destino inicial (Destino1)
                        navigateToMainActivity()

                    },
                    onRegisterClick = {}
                )
            }
        }
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val areFieldsEmpty = emailState.value.isEmpty() || passwordState.value.isEmpty()
    val errorMessageState = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
        ) {
            // Imagen del logo
            Image(
                painter = painterResource(id = R.drawable.seta),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Título
            Text(
                text = "UrFungi",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)

            )

            // Campos de texto
            TextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            TextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Mensaje de error
            errorMessageState.value?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            // Botón de inicio de sesión
            Button(
                onClick = {
                    // Verificar si los campos están vacíos
                    if (areFieldsEmpty) {
                        errorMessageState.value = "Por favor, rellena todos los campos."
                    } else {
                        // Utilizar la autenticación de Firebase para iniciar sesión
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            emailState.value,
                            passwordState.value
                        ).addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                // El inicio de sesión fue exitoso
                                onLoginSuccess()

                            } else {
                                // Hubo un error en el inicio de sesión, mostrar mensaje de error
                                errorMessageState.value =
                                    signInTask.exception?.message ?: "Error al iniciar sesión"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Iniciar sesión")
            }

            // Botón de registro
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Registrar")
            }
        }

    }

}

