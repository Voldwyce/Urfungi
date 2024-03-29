package com.example.urfungi.Usuarios

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.urfungi.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedButton
import com.example.urfungi.MainActivity
import com.example.urfungi.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


class LoginAppActivity : ComponentActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private val googleSignInClient by lazy {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private val RC_SIGN_IN = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Si el usuario ya ha iniciado sesión, navegar directamente al MainActivity
            navigateToMainActivity()
        } else {

            googleSignInLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    Log.d("GoogleSignIn", "ActivityResultLauncher activado")
                    if (result.resultCode == Activity.RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            firebaseAuthWithGoogle(account.idToken!!)
                        } catch (e: ApiException) {
                            Log.e("GoogleSignIn", "Error al iniciar sesión con Google", e)
                        }
                    }
                }

            setContent {

                AppTheme {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                onLoginSuccess = {
                                    // Navegar al MainActivity con el destino inicial (Destino1)
                                    navigateToMainActivity()

                                },
                                onRegisterClick = {
                                    navController.navigate("register")
                                },

                                googleSignInClient = googleSignInClient,
                                googleSignInLauncher = googleSignInLauncher
                            )
                        }
                        composable("register") {
                            RegistrationScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val RC_SIGN_IN = 9001
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, actualizar UI con la información del usuario
                    val user = FirebaseAuth.getInstance().currentUser
                } else {
                    // Si el inicio de sesión falla, mostrar un mensaje al usuario.
                }
            }
    }


}

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    googleSignInClient: GoogleSignInClient,
    googleSignInLauncher: ActivityResultLauncher<Intent>
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisibility by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del logo
            Image(
                painter = painterResource(id = R.drawable.login_mushroom),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )

            // Campos de texto
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisibility) "Ocultar contraseña" else "Mostrar contraseña"
                        )

                    }
                }
            )

            // Mensaje de error
            errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón de inicio de sesión
            Button(
                onClick = {
                    // Verificar si los campos están vacíos
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Por favor, rellena todos los campos."
                    } else {
                        // Utilizar la autenticación de Firebase para iniciar sesión
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            email,
                            password
                        ).addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                // El inicio de sesión fue exitoso
                                onLoginSuccess()
                            } else {
                                // Hubo un error en el inicio de sesión, mostrar mensaje de error
                                errorMessage =
                                    signInTask.exception?.message ?: "Error al iniciar sesión"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            ) {
                Text("Iniciar sesión")
            }
            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar sesión con Google")
            }
            Spacer(modifier = Modifier.padding(4.dp))
            // Botón de registro
            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        errorMessage = "Se ha enviado un correo electrónico de restablecimiento de contraseña a $email"
                        sendPasswordResetEmail(email, errorMessage)
                    } else {
                        errorMessage = "Por favor, ingresa tu correo electrónico."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                Text("Olvidé mi contraseña")
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Registrar")
            }
        }
    }
}

@Composable
fun RegistrationScreen(
    navController: NavController
) {
    val auth = Firebase.auth

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        OutlinedTextField(
            value = birthdate,
            onValueChange = { birthdate = it },
            label = { Text("Fecha de nacimiento") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.length < 6 || !isAlphaNumeric(password)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword != password
        )

        if (errorMessage?.isNotEmpty() == true) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                // Validar la entrada
                if (name.isEmpty() || username.isEmpty() || birthdate.isEmpty() || email.isEmpty() || password.length < 6 || !isAlphaNumeric(
                        password
                    ) || confirmPassword != password
                ) {
                    errorMessage = "Por favor, verifica los campos ingresados."
                } else if (!isValidEmail(email)) {
                    errorMessage = "Por favor, ingresa un correo electrónico válido."
                } else {
                    // Comprobar si el nombre de usuario ya existe
                    checkAvailability(username, email) { isAvailable ->
                        if (isAvailable) {
                            // Resto del código para registrar el usuario
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val userId = user?.uid ?: ""

                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { emailTask ->
                                                if (emailTask.isSuccessful) {
                                                    // Enviar correo de verificación
                                                    errorMessage =
                                                        "Se ha enviado un correo de verificación a $email"
                                                } else {
                                                    errorMessage =
                                                        "Error al enviar el correo de verificación: ${emailTask.exception?.message}"
                                                }
                                            }

                                        // Crear un mapa con los datos del usuario
                                        val userData = hashMapOf(
                                            "id" to userId,
                                                "nombre" to name,
                                            "username" to username,
                                            "fechaNacimiento" to birthdate,
                                            "email" to email,
                                            "totalFallos" to 0,
                                            "totalAciertos" to 0,
                                            "foto" to "https://firebasestorage.googleapis.com/v0/b/urfungui.appspot.com/o/usuarios%2Fsmurf.jpg?alt=media&token=a865ccdc-9b7b-48b9-b816-830b2540a8a4",
                                            "amigos" to emptyList<String>(),
                                            "solicitudAmistad" to emptyList<String>()

                                        )

                                        // Agregar los datos a Firestore
                                        val db = Firebase.firestore
                                        db.collection("usuarios")
                                            .document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                // Registro exitoso, navegar a la pantalla de inicio de sesión
                                                navController.popBackStack(
                                                    "login",
                                                    inclusive = false
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                // Mostrar mensaje de error al guardar en Firestore
                                                errorMessage =
                                                    "Error al guardar datos en Firestore: ${e.message}"
                                            }
                                    } else {
                                        // Mostrar mensaje de error al registrar en Firebase Auth
                                        errorMessage =
                                            "El nombre de usuario o correo electrónico ya está en uso."
                                    }
                                }
                        } else {
                            errorMessage = "El nombre de usuario ya está en uso."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.Registrar_boton))
        }


        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.Cancelar_boton))
        }
    }
}

private fun isAlphaNumeric(text: String): Boolean {
    return text.all { it.isLetterOrDigit() }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun checkAvailability(username: String, email: String, callback: (Boolean) -> Unit) {
    val db = Firebase.firestore
    db.collection("usuarios")
        .whereEqualTo("username", username)
        .get()
        .addOnCompleteListener { task ->
            val isUsernameAvailable = task.isSuccessful && (task.result?.isEmpty == true)

            // Comprobar también la disponibilidad del correo electrónico
            db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener { emailTask ->
                    val isEmailAvailable =
                        emailTask.isSuccessful && (emailTask.result?.isEmpty == true)

                    // Llamar al callback con el resultado combinado
                    callback(isUsernameAvailable && isEmailAvailable)
                }
        }
}

private fun sendPasswordResetEmail(email: String, errorMessage: String?) {
    val auth = FirebaseAuth.getInstance()
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
            }
        }
}