package com.example.urfungi

import com.example.urfungi.MapsPosts.MapScreen
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.urfungi.Curiosidades.SetasListScreen
import com.example.urfungi.MapsPosts.SearchScreen
import com.example.urfungi.Posts.Post
import com.example.urfungi.Posts.UserPostsScreen
import com.example.urfungi.QuizJuego.HighscoresScreen
import com.example.urfungi.QuizJuego.QuizPostScreen
import com.example.urfungi.QuizJuego.QuizScreenFromFirebase
import com.example.urfungi.Recetas.RecetasSetasListScreen
import com.example.urfungi.Repo.Creditos
import com.example.urfungi.Repo.Repositorio
import com.example.urfungi.Repo.WeatherApp
import com.example.urfungi.Repo.Creditos
import com.example.urfungi.Repo.WeatherApp
import com.example.urfungi.Restaurantes.RestaurantesSetasListScreen
import com.example.urfungi.Usuarios.LoginAppActivity
import com.example.urfungi.Usuarios.MensajesChat
import com.example.urfungi.Usuarios.usuarios
import com.example.urfungi.Usuarios.Chat
import com.example.urfungi.Usuarios.ChatGrupo
import com.example.urfungi.Usuarios.EditarGrupoScreen
import com.example.urfungi.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//import androidx.compose.material.rememberCoilPainter


class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    val database: DatabaseReference = Firebase.database.reference

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        /*
        uploadJsonToFirebase()
        */

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verificar si el usuario está logeado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // El usuario no está logeado, navegar a la pantalla de login

        } else {
            setContent {
                AppTheme {
                    val navController = rememberNavController()

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val destinoActual = navBackStackEntry?.destination


                                Destino.entries.forEach { destino ->
                                    val destinoSeleccionado =
                                        destinoActual?.hierarchy?.any { it.route == destino.ruta } == true

                                    NavigationBarItem(
                                        selected = destinoSeleccionado,
                                        onClick = {
                                            if (destino.nombre == R.string.destino3) {
                                                navController.navigate(Destino.Destino3.ruta) {
                                                    popUpTo(navController.graph.findStartDestination().id)
                                                    launchSingleTop = true
                                                }
                                            } else if (!destinoSeleccionado) {
                                                navController.navigate(destino.ruta) {
                                                    popUpTo(navController.graph.findStartDestination().id)
                                                    launchSingleTop = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (destinoSeleccionado) destino.iconoSeleccionado else destino.icono,
                                                contentDescription = stringResource(id = destino.nombre)
                                            )
                                        },
                                        label = {
                                            Text(text = stringResource(id = destino.nombre))
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Destino.Destino3.ruta
                            ) {
                                composable(
                                    route = Destino.Destino1.ruta,
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) {
                                    SearchScreen()
                                }

                                composable(
                                    route = "mensajes",
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) {
                                    MensajesScreen(navController = navController)
                                }

                                composable(
                                    route = "grupoCrear",
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) {
                                    CrearGrupoScreen(navController = navController)
                                }

                                composable(
                                    route = "grupoChat/{grupoId}/{Admin}/{nombreGrupo}",
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) { backStackEntry ->
                                    val grupoId = backStackEntry.arguments?.getString("grupoId")
                                    val Admin = backStackEntry.arguments?.getString("Admin")
                                    val Nombre = backStackEntry.arguments?.getString("nombre")


                                    ChatGrupo(grupoId = grupoId,Admin = Admin, nombre = Nombre, navController = navController)
                                }

                                composable(
                                    route = "editarGrupo/{grupoId}",
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) { backStackEntry ->
                                    val grupoId = backStackEntry.arguments?.getString("grupoId")

                                    EditarGrupoScreen(navController, grupoId)
                                }

                                composable(
                                    route = "mensajes/{usuarioId}/{username}/{escapedUriString}",
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) { backStackEntry ->
                                    val usuarioId = backStackEntry.arguments?.getString("usuarioId")
                                    val username = backStackEntry.arguments?.getString("username")
                                    val foto = backStackEntry.arguments?.getString("escapedUriString")


                                    if (usuarioId != null && username != null && foto != null) {
                                        // Aquí puedes cargar la pantalla MensajesChat con el usuario correspondiente
                                        MensajesChat(usuarioId = usuarioId, username = username, imagen = foto)
                                    } else {
                                        // Manejar el caso en el que no se proporciona el ID del usuario o el nombre de usuario
                                        // Puedes mostrar un mensaje de error o volver a la pantalla anterior
                                    }
                                }
                                composable(
                                    route = Destino.Destino2.ruta,
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stringResource(id = Destino.Destino2.nombre))
                                    }
                                    MapScreen(navController, null, null)
                                }

                                composable(
                                    "mapScreen/{lat}/{lon}",
                                    arguments = listOf(
                                        navArgument("lat") { type = NavType.FloatType },
                                        navArgument("lon") { type = NavType.FloatType }
                                    )
                                ) { backStackEntry ->
                                    val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble()
                                    val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble()
                                    MapScreen(navController, lat, lon)
                                }
                                composable(
                                    route = Destino.Destino3.ruta,
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    }
                                ) {
                                    Scaffold(
                                        topBar = {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(0.dp)
                                            ) {
                                                TopAppBar(
                                                    title = {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(text = "UrFungi")
                                                        }
                                                    },
                                                    navigationIcon = {
                                                        IconButton(onClick = {
                                                            navController.navigate(DestinoJuego.Destinojuego.ruta)
                                                        }) {
                                                            Icon(
                                                                Icons.Filled.PlayArrow,
                                                                contentDescription = "Navigation Icon"
                                                            )
                                                        }
                                                    },
                                                    actions = {
                                                        IconButton(onClick = {
                                                            navController.navigate("mensajes")
                                                            /* Handle send message icon press */
                                                        }) {
                                                            Icon(
                                                                Icons.Filled.MailOutline,
                                                                contentDescription = "Send Message Icon"
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    ) { paddingValues ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = paddingValues.calculateTopPadding())
                                        ) {
                                            AllPostsScreen()
                                        }
                                    }
                                }

                                composable("setas_list_item") {
                                    SetasListScreen()
                                }

                                composable("recetas_list_item") {
                                    RecetasSetasListScreen()
                                }

                                composable("restaurantes_list_item") {
                                    RestaurantesSetasListScreen(navController)
                                }

                                composable(
                                    route = Destino.Destino4.ruta,
                                    enterTransition = {
                                        when (initialState.destination.route) {
                                            Destino.Destino3.ruta -> slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            )

                                            else -> slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            )
                                        }
                                    },
                                    exitTransition = {
                                        when (targetState.destination.route) {
                                            Destino.Destino3.ruta -> slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            )

                                            else -> slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            )
                                        }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 16.dp, top = 40.dp, bottom = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        UserPostsScreen(navController)
                                    }
                                }

                                composable("weather_screen") {
                                    WeatherApp()
                                }

                                composable("creditos") {
                                    Creditos()
                                }

                                composable(
                                    route = Destino.Destino5.ruta,
                                    enterTransition = {
                                        when (initialState.destination.route) {
                                            Destino.Destino4.ruta -> slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            )

                                            else -> slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            )
                                        }
                                    },
                                    exitTransition = {
                                        when (targetState.destination.route) {
                                            Destino.Destino4.ruta -> slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            )

                                            else -> slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            )
                                        }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 16.dp, top = 40.dp, bottom = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Repositorio(navController)
                                    }
                                }
                                composable(
                                    route = DestinoJuego.Destinojuego.ruta,
                                    enterTransition = {
                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        QuizScreenFromFirebase(
                                            onHighscoresClicked = { navController.navigate("highscores") },
                                            onQuizPostsClicked = { navController.navigate("quizPosts") }

                                        )
                                    }
                                }
                                composable("highscores") {
                                    HighscoresScreen()
                                }

                                composable("quizPosts") {
                                    QuizPostScreen()
                                }
                            }
                        }

                    }
                }
            }
            requestPermissions()
        }

    }

    private fun requestPermissions() {
        requestCameraPermissionIfMissing { cameraGranted ->
            requestLocationPermissionsIfMissing { locationGranted ->
                if (cameraGranted && locationGranted) {
                    Toast.makeText(this, "Todos los permisos aceptados!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "No se han aceptado todos los permisos!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun requestLocationPermissionsIfMissing(onResult: ((Boolean) -> Unit)) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
        } else {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all {
                    it.value == true
                }
                onResult(granted)
            }.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestCameraPermissionIfMissing(onResult: ((Boolean) -> Unit)) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
            onResult(true)
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                onResult(it)
            }.launch(Manifest.permission.CAMERA)

    }

    @Composable
    fun MensajesScreen(navController: NavController) {
        var usuario by remember { mutableStateOf<usuarios?>(null) }
        var amigos by remember { mutableStateOf<List<usuarios>>(emptyList()) }
        var solicitudes by remember { mutableStateOf<List<usuarios>>(emptyList()) }
        var explorar by remember { mutableStateOf<List<usuarios>>(emptyList()) }
        var listaActual by remember { mutableStateOf<List<usuarios>>(emptyList()) }
        var listaActualChat by remember { mutableStateOf<List<Chat>>(emptyList()) }
        var solicitudesRecibidas = mutableListOf<usuarios>()
        var solicitudesEnviadas = mutableListOf<usuarios>()
        var clicUsuario by remember { mutableStateOf(false) }
        var showCreateGroupButton by remember { mutableStateOf(false) }
        var grupos by remember { mutableStateOf<List<Chat>>(emptyList()) }
        var mostrarGrupos by remember { mutableStateOf(false) }
        var isFriend by remember { mutableStateOf(false) }
        var isChat by remember { mutableStateOf(false) }
        var showProfileDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val db = Firebase.firestore
                val userReference = db.collection("usuarios").document(userId)

                userReference.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // El documento existe, puedes obtener el objeto usuarios
                        usuario = documentSnapshot.toObject(usuarios::class.java)

                        // Obtener la lista de amigos
                        val listaAmigos = usuario?.amigos ?: emptyList()

                        // Obtener la lista de solicitudes
                        val listaSolicitud = usuario?.solicitudAmistad ?: emptyList()

                        /*
                        * Filtros para la lista de amigos:
                        * 1. Tiene que estar en el del usuario en amigos
                        * */

                        if (listaAmigos.isNotEmpty()) {
                            // Obtener información de cada amigo y almacenarla en la lista 'amigos'
                            listaAmigos.forEach { amigoId ->
                                val amigoReference = db.collection("usuarios").document(amigoId)
                                amigoReference.get().addOnSuccessListener { amigoSnapshot ->
                                    if (amigoSnapshot.exists()) {
                                        val amigo = amigoSnapshot.toObject(usuarios::class.java)
                                        if (amigo != null) {
                                            amigos = amigos + amigo
                                            // Actualizar la listaActual después de obtener la información del amigo
                                            listaActual = amigos
                                        }
                                    }
                                }
                            }
                        } else {
                            listaActual = emptyList()
                        }

                        /*
                        * Filtros para la lista solicitudes:
                        * 1. Si envias solicitud de amistad a un usuario se guardara el id del usuario
                        * en solicitudesAmistad
                        * 2. A la hora de mostrar la lista primero muestra a los usuarios que tienen tu usuario
                        * en su solicitudesAmistad, en estos casos tiene que salir un boton para aceptar o rechazar,
                        * despues mostrara los usuarios de tu solicitudesAmistad, que servira para saber a quien
                        * le has enviado solicitud
                        * */


                        // Obtener las solicitudes recibidas
                        val currentUserID = usuario?.id ?: ""
                        db.collection("usuarios")
                            .whereArrayContains("solicitudAmistad", currentUserID)
                            .get()
                            .addOnSuccessListener { result ->
                                solicitudesRecibidas = result.toObjects(usuarios::class.java)

                                if (solicitudesRecibidas.isNotEmpty()) {
                                    solicitudes = solicitudesRecibidas
                                    listaActual = solicitudes
                                } else {
                                    listaActual = emptyList()
                                }
                            }
                            .addOnFailureListener { e ->
                                // Manejar errores aquí
                                Log.e(
                                    TAG,
                                    "Error al obtener la lista de solicitudesRecibidas en Firestore: $e"
                                )
                            }

                        usuario?.id?.let { userId ->
                            val db = Firebase.firestore
                            db.collection("Chat")
                                .whereArrayContains("integrantes", userId)
                                .whereEqualTo("grupo", true) // Agregar esta condición para comprobar si grupo es true
                                .get()
                                .addOnSuccessListener { result ->
                                    grupos = result.toObjects(Chat::class.java)
                                    listaActualChat = grupos
                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores aquí
                                    Log.e(TAG, "Error al obtener la lista de grupos en los que el usuario es un integrante: $e")
                                }
                        }
                        /*
                        * Filtros para la lista de exploración:
                        * 1. Esta lista tiene en cuenta todas las otras listas para mostrar a los usuarios.
                        * 2. No deben aparecer amigos en esta lista
                        * 3. No deben de aparecer usuarios que hayan enviado solicitud o usuarios a los que
                        * hayas enviado solicitud
                        */

                        // Crear conjuntos de IDs de amigos y solicitudes para facilitar la comparación
                        val setAmigos = listaAmigos.toSet()
                        val setSolicitudes = listaSolicitud.toSet()

                        // Cargar la lista completa de usuarios como exploración excluyendo al usuario actual, amigos y solicitudes
                        if ((listaSolicitud.isNotEmpty()) && (listaAmigos.isNotEmpty())) {
                            db.collection("usuarios")
                                .whereNotIn(
                                    FieldPath.documentId(),
                                    listaSolicitud + listaAmigos
                                ) // Excluir usuarios en la lista de solicitudes y amigos
                                .get()
                                .addOnSuccessListener { result ->
                                    explorar = result.toObjects(usuarios::class.java)

                                    // Filtrar la lista para excluir al usuario actual, amigos y solicitudes
                                    explorar =
                                        explorar.filterNot { it.id == userId || it.id in setAmigos || it.id in setSolicitudes }

                                    // Actualizar la listaActual después de obtener la información de explorar
                                    listaActual = explorar
                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores aquí
                                    Log.e(
                                        TAG,
                                        "Error al obtener la lista de exploración en Firestore: $e"
                                    )
                                }
                        } else if ((listaSolicitud.isEmpty()) && (listaAmigos.isNotEmpty())) {
                            db.collection("usuarios")
                                .whereNotIn(
                                    FieldPath.documentId(),
                                    listaAmigos
                                ) // Excluir usuarios en la lista de amigos
                                .get()
                                .addOnSuccessListener { result ->
                                    explorar = result.toObjects(usuarios::class.java)

                                    // Filtrar la lista para excluir al usuario actual y amigos
                                    explorar =
                                        explorar.filterNot { it.id == userId || it.id in setAmigos }

                                    // Actualizar la listaActual después de obtener la información de explorar
                                    listaActual = explorar
                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores aquí
                                    Log.e(
                                        TAG,
                                        "Error al obtener la lista de exploración en Firestore: $e"
                                    )
                                }
                        } else if ((listaAmigos.isEmpty()) && (listaSolicitud.isNotEmpty())) {
                            db.collection("usuarios")
                                .whereNotIn(
                                    FieldPath.documentId(),
                                    listaSolicitud
                                ) // Excluir usuarios en la lista de solicitudes
                                .get()
                                .addOnSuccessListener { result ->
                                    explorar = result.toObjects(usuarios::class.java)

                                    // Filtrar la lista para excluir al usuario actual y solicitudes
                                    explorar =
                                        explorar.filterNot { it.id == userId || it.id in setSolicitudes }

                                    // Actualizar la listaActual después de obtener la información de explorar
                                    listaActual = explorar
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        TAG,
                                        "Error al obtener la lista de exploración en Firestore: $e"
                                    )
                                }
                        } else {
                            db.collection("usuarios")
                                .get()
                                .addOnSuccessListener { result ->
                                    explorar = result.toObjects(usuarios::class.java)

                                    // Filtrar la lista para excluir al usuario actual
                                    explorar = explorar.filterNot { it.id == userId }

                                    // Actualizar la listaActual después de obtener la información de explorar
                                    listaActual = explorar
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        TAG,
                                        "Error al obtener la lista de exploración en Firestore: $e"
                                    )
                                }
                        }

                    } else {
                        // El documento no existe
                        Log.d(
                            TAG,
                            "No se encontró el documento en Firestore para el usuario con ID: $userId"
                        )
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener datos del usuario en Firestore: $e")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            // Texto "Tu Perfil"
            Text(
                text = "Tu Perfil:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tarjeta de información del usuario
                if (usuario != null) {
                    MensajesCard(
                        usuario = usuario,
                        onLogoutClick = {
                            FirebaseAuth.getInstance().signOut()
                            navigateToLoginAppActivity()
                        },
                        onCrearGrupoClick = {
                            navController.navigate("grupoCrear")
                        },
                        onClick = { showProfileDialog = true }

                    )
                    if (showProfileDialog) {
                        // Mostrar la pestaña de perfil
                        ProfileDialog(
                            usuario = usuario,
                            onCloseDialog = { showProfileDialog = false } // Cerrar el diálogo al hacer clic fuera de él
                        )
                    }
                } else {
                    Text("El usuario es nulo")
                }
            }
            // Botones superiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    onClick = {
                        listaActual = explorar
                        mostrarGrupos = false
                        isFriend = false
                        isChat = false

                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Explorar")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    onClick = {
                        listaActual = amigos
                        mostrarGrupos = false
                        isFriend = true
                        isChat = false

                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Amigos")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    onClick = {
                        listaActual = solicitudes
                        mostrarGrupos = false
                        isFriend = false
                        isChat = false

                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Solicitud")
                }
            }

            // Botones inferiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    onClick = {
                        listaActual = amigos
                        clicUsuario = true
                        showCreateGroupButton = false
                        mostrarGrupos = false
                        isFriend = false
                        isChat = true

                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Chat")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
                    ),
                    onClick = {
                        showCreateGroupButton = true
                        listaActualChat = grupos
                        mostrarGrupos = true
                        isFriend = false
                        isChat = false

                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Grupos")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(400.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Lista de tarjetas de amigos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 5.dp)
                ) {

                    if (listaActual.isNotEmpty()) {
                        if (mostrarGrupos) {

                            items(listaActualChat) { grupos ->
                                GrupoCard(
                                    grupo = grupos,
                                    navController = navController,
                                )
                            }
                        } else {
                            items(listaActual) { amigo ->


                                AmigoCard(
                                    amigo = amigo,
                                    isExploring = (listaActual === explorar),
                                    isFriend = isFriend,
                                    isChat = isChat,
                                    onAddClick = {
                                        // Agregar amigo.id a la lista de solicitudesAmistad del usuario actual
                                        usuario?.let { currentUser ->
                                            val db = Firebase.firestore
                                            val userReference =
                                                db.collection("usuarios")
                                                    .document(currentUser.id ?: "")

                                            // Actualizar la lista de solicitudesAmistad en Firestore
                                            userReference.update(
                                                "solicitudAmistad",
                                                FieldValue.arrayUnion(it)
                                            )
                                                .addOnSuccessListener {
                                                    // Éxito al agregar a las solicitudesAmistad
                                                }
                                                .addOnFailureListener { e ->
                                                    // Manejar errores aquí
                                                    Log.e(
                                                        TAG,
                                                        "Error al agregar a las solicitudesAmistad: $e"
                                                    )
                                                }
                                        }
                                    },
                                    navController = navController,
                                    showAdditionalButton = (listaActual === solicitudes), // Mostrar el botón adicional solo en la lista de solicitudes
                                    onAdditionalButtonClick = { amigoSeleccionado ->
                                        // Lógica para agregar al usuario amigo a la lista de amigos del usuario logeado
                                        usuario?.let { currentUser ->
                                            val db = Firebase.firestore

                                            // Actualizar la lista de amigos en Firestore para el usuario logeado
                                            val userReference = db.collection("usuarios")
                                                .document(currentUser.id ?: "")
                                            userReference.update(
                                                "amigos",
                                                FieldValue.arrayUnion(amigoSeleccionado.id)
                                            )
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener { e ->
                                                    // Manejar errores aquí
                                                    Log.e(
                                                        TAG,
                                                        "Error al agregar a la lista de amigos: $e"
                                                    )
                                                }

                                            // Actualizar la lista de amigos en Firestore para el usuario amigo
                                            val amigoReference = db.collection("usuarios")
                                                .document(amigoSeleccionado.id ?: "")
                                            amigoReference.update(
                                                "amigos",
                                                FieldValue.arrayUnion(currentUser.id)
                                            )
                                                .addOnSuccessListener {
                                                    // Lógica para eliminar el id del usuario logeado de la lista de solicitudamigos del usuario amigo
                                                    val amigoReference =
                                                        db.collection("usuarios").document(amigo.id)
                                                    amigoReference.update(
                                                        "solicitudAmistad",
                                                        FieldValue.arrayRemove(currentUser.id)
                                                    )
                                                        .addOnSuccessListener {
                                                            // Lógica para eliminar al usuario de la lista de solicitudes del usuario logeado
                                                            userReference.update(
                                                                "solicitudAmistad",
                                                                FieldValue.arrayRemove(amigo.id)
                                                            )
                                                                .addOnSuccessListener {
                                                                    // Éxito al eliminar de las solicitudesAmistad
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    // Manejar errores aquí
                                                                    Log.e(
                                                                        TAG,
                                                                        "Error al eliminar de las solicitudesAmistad: $e"
                                                                    )
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Manejar errores aquí
                                                            Log.e(
                                                                TAG,
                                                                "Error al eliminar la solicitudAmistad: $e"
                                                            )
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    // Manejar errores aquí
                                                    Log.e(
                                                        TAG,
                                                        "Error al agregar a la lista de amigos del usuario amigo: $e"
                                                    )
                                                }
                                        }
                                    },
                                    onRejectClick = { usuarioRechazado ->
                                        usuario?.let { currentUser ->
                                            val db = Firebase.firestore
                                            // Lógica para eliminar el id del usuario logeado de la lista de solicitudamigos del usuario amigo
                                            val amigoReference =
                                                db.collection("usuarios").document(amigo.id)
                                            amigoReference.update(
                                                "solicitudAmistad",
                                                FieldValue.arrayRemove(currentUser.id)
                                            )
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener { e ->
                                                    // Manejar errores aquí
                                                    Log.e(
                                                        TAG,
                                                        "Error al eliminar la solicitudAmistad: $e"
                                                    )
                                                }
                                        }
                                    },
                                    onEliminarClick = { amigoEliminado ->
                                        // Lógica para eliminar al amigo de la lista de amigos del usuario logeado

                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser != null) {

                                        val db = Firebase.firestore
                                        val userId = currentUser.uid

                                        // Actualizar la lista de amigos en Firestore para el usuario logeado
                                        val userReference =
                                            db.collection("usuarios").document(userId ?: "")
                                        userReference.update(
                                            "amigos",
                                            FieldValue.arrayRemove(amigoEliminado.id)
                                        )
                                            .addOnSuccessListener {
                                                // Éxito al eliminar de la lista de amigos
                                            }
                                            .addOnFailureListener { e ->
                                                // Manejar errores aquí
                                                Log.e(
                                                    TAG,
                                                    "Error al eliminar de la lista de amigos: $e"
                                                )
                                            }

                                        // Actualizar la lista de amigos en Firestore para el usuario amigo
                                        val amigoReference = db.collection("usuarios")
                                            .document(amigoEliminado.id ?: "")
                                        amigoReference.update(
                                            "amigos",
                                            FieldValue.arrayRemove(userId)
                                        )
                                            .addOnSuccessListener {
                                                // Éxito al eliminar de la lista de amigos del usuario amigo
                                            }
                                            .addOnFailureListener { e ->
                                                // Manejar errores aquí
                                                Log.e(
                                                    TAG,
                                                    "Error al eliminar de la lista de amigos del usuario amigo: $e"
                                                )
                                            }
                                    } },                                )
                            }
                        }
                    } else {
                        // Mostrar mensaje cuando la lista esté vacía
                        item {
                            Text(
                                text = when {
                                    listaActual === amigos -> "Sin Amigos"
                                    listaActual === amigos && isChat === true -> "Sin Chats"
                                    listaActual === solicitudes -> "Sin Solicitudes"
                                    mostrarGrupos === true && listaActualChat === grupos -> "Sin Grupos"
                                    else -> "Exploración Completada"
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }

            }
        }
    }


    @Composable
    fun AmigoCard(
        amigo: usuarios,
        isExploring: Boolean,
        isFriend: Boolean,
        isChat: Boolean,
        onAddClick: (String) -> Unit,
        navController: NavController,
        showAdditionalButton: Boolean = false, // Nuevo parámetro para controlar la visibilidad del botón adicional
        onAdditionalButtonClick: (usuarios) -> Unit = {},
        onRejectClick: (usuarios) -> Unit = {},// Nuevo parámetro para manejar el clic en el botón adicional
        onEliminarClick: (usuarios) -> Unit = {}
        ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(10.dp)
                .clickable {
                    if (isChat) {
                        // Navegar a la pantalla MensajesChat cuando isChat es true
                        val escapedUriString = URLEncoder.encode(amigo.foto, "UTF-8")
                        navController.navigate("mensajes/${amigo.id}/${amigo.username}/${escapedUriString}")
                    }
                    // Si isChat es false, no hacer nada al hacer clic en la tarjeta
                },
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen de perfil del amigo a la izquierda
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            val amigoFoto = amigo.foto
                            val imageUrl = "$amigoFoto"
                            Glide.with(context)
                                .load(imageUrl)
                                .fitCenter()
                                .transform(CircleCrop())
                                .into(this)
                        }
                    }, modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Green, CircleShape)
                )

                // Espaciador horizontal
                Spacer(modifier = Modifier.width(12.dp))

                // Columna para el Usuario y Nombre del amigo
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Nombre de usuario

                    Text(
                        text = amigo.username ?: "Username",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )

                    // Nombre completo del amigo
                    Text(
                        text = amigo.nombre ?: "Nombre del Amigo",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                // Espaciador horizontal
                Spacer(modifier = Modifier.width(16.dp))

                // Botones a la derecha del todo
                if (isExploring) {

                    // Botón de Agregar

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                            .background(Color.Green)
                            .clickable { onAddClick(amigo.id) }, // Acción al hacer clic
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Agregar",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                }

                if (isFriend) {

                    // Botón adicional para eliminarAmigo
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red) //
                            .clickable{ onEliminarClick(amigo) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Rechazar",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                }

                if (showAdditionalButton) {
                    //Boton Solicitud Aceptar
                    Box(
                            modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Green)
                            .clickable{ onAdditionalButtonClick(amigo) },
                        contentAlignment = Alignment.Center

                    ) {
                        Icon(
                            imageVector = Icons.Default.Add, // Reemplaza con el icono adecuado
                            contentDescription = "Botón Agregar",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botón adicional para rechazar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red) //
                            .clickable{ onRejectClick(amigo) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Rechazar",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                }
            }
        }
    }

    // Función para obtener la ubicación del restaurante desde Firebase
    fun obtenerUbicacionesRestaurantes(
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val firestore = Firebase.firestore
            val restaurantesRef = firestore.collection("restaurantes")
            restaurantesRef.get()
                .addOnSuccessListener { snapshot ->
                    val ubicaciones = mutableListOf<Pair<String, String>>()
                    for (document in snapshot.documents) {
                        val longitud = document.getString("longitud")
                        val latitud = document.getString("latitud")
                        if (longitud != null && latitud != null) {
                            ubicaciones.add(Pair(longitud, latitud))
                        }
                    }
                    onSuccess(ubicaciones)
                }
                .addOnFailureListener { exception ->
                    onError("Error al obtener las ubicaciones de los restaurantes: ${exception.message}")
                }
        } catch (e: Exception) {
            onError("Error al obtener las ubicaciones de los restaurantes: ${e.message}")
        }
    }


    @Composable
    fun MensajesCard(usuario: usuarios?, onLogoutClick: () -> Unit, onCrearGrupoClick: () -> Unit,onClick: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            usuario?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Aumentar la altura del Card
                        .clickable { onClick()},
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp), // Aumentar el padding

                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen de perfil del usuario a la izquierda
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    val userFoto = usuario.foto
                                    val imageUrl = "$userFoto"
                                    Glide.with(context)
                                        .load(imageUrl)
                                        .fitCenter()
                                        .transform(CircleCrop())
                                        .into(this)
                                }
                            }, modifier = Modifier
                                .size(90.dp) // Aumentar el tamaño de la imagen de perfil
                                .clip(CircleShape)
                                .border(2.dp, Color.Green, CircleShape)
                        )

                        // Espaciador horizontal
                        Spacer(modifier = Modifier.width(12.dp))

                        // Columna para el Usuario y Nombre
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically),
                            verticalArrangement = Arrangement.spacedBy(4.dp) // Aumentar el espacio entre los textos
                        ) {
                            // Nombre de usuario más grande
                            Text(
                                text = usuario.username ?: "Username",
                                fontSize = 24.sp, // Aumentar el tamaño de la fuente
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Nombre completo del usuario
                            Text(
                                text = usuario.nombre ?: "Nombre del Usuario",
                                fontSize = 14.sp, // Aumentar el tamaño de la fuente
                                color = Color.White
                            )
                        }

                        // Espaciador horizontal
                        Spacer(modifier = Modifier.width(16.dp))

                        // Botones a la derecha del todo
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 8.dp) // Alineación a la derecha
                        ) {

                            Spacer(modifier = Modifier.height(10.dp))

                            // Botón de LogOut
                            Box(
                                modifier = Modifier
                                    .size(36.dp) // Tamaño del botón
                                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                                    .background(Color.Red)
                                    .clickable { onLogoutClick() }, // Acción al hacer clic
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Botón de crear grupo
                            Box(
                                modifier = Modifier
                                    .size(36.dp) // Mismo tamaño que el botón de logout
                                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                                    .background(Color.Green)
                                    .clickable { onCrearGrupoClick() }, // Acción al hacer clic
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Crear Grupo",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLoginAppActivity() {
        val intent = Intent(this, LoginAppActivity::class.java)
        startActivity(intent)
        finish()
    }


    /*
    private fun uploadJsonToFirebase() {
        val jsonString = getJsonDataFromAsset(this, "setas.json")
        if (jsonString != null) {
            val listType = object : TypeToken<List<Setas>>() {}.type
            val setas: List<Setas> = Gson().fromJson(jsonString, listType)

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val db = Firebase.firestore
                setas.forEach { seta ->
                    val setaData = hashMapOf(
                        "id" to seta.id,
                        "Nombre" to seta.Nombre,
                        "NombreCientifico" to seta.NombreCientifico,
                        "Familia" to seta.Familia,
                        "Temporada" to seta.Temporada,
                        "Imagen" to seta.Imagen,
                        "Comestible" to seta.Comestible,
                        "Toxicidad" to seta.Toxicidad,
                        "Descripcion" to seta.Descripcion,
                        "Habitat" to seta.Habitat,
                        "Dificultad" to seta.Dificultad,
                        "Curiosidades" to seta.Curiosidades
                    )

                    db.collection("setas")
                        .document(seta.Nombre)
                        .set(setaData)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firebase", "Error al guardar datos en Firestore: ${e.message}")
                        }
                }
            } else {
            }
        }
    }
    */

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
    }
}

@Composable
fun AllPostsScreen() {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var posts by remember { mutableStateOf<List<Pair<String, Post>>>(emptyList()) }
    var friends by remember { mutableStateOf<List<String>>(emptyList()) }
    var username by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            val userRef = db.collection("usuarios").document(currentUser.uid)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(usuarios::class.java)
                username = user?.username
                friends = user?.amigos ?: emptyList()

                val postsRef = db.collection("posts")
                postsRef.addSnapshotListener { querySnapshot, _ ->
                    val allPosts = querySnapshot?.documents?.mapNotNull { document ->
                        val post = document.toObject(Post::class.java)
                        if (post != null) Pair(document.id, post) else null
                    } ?: emptyList()

                    posts = allPosts.filter { postPair ->
                        postPair.second.usuario != currentUser.uid && (postPair.second.privacidad == "Publico" || (postPair.second.privacidad == "Amigos" && friends.contains(
                            postPair.second.usuario
                        )))
                    }
                }
            }
        }
    }
    LazyColumn {
        item { /* Header content */ }
        items(posts) { postPair ->
            PostCard(postPair = postPair, onLikeClick = ::onLikeClick, userUsername = username)
        }
        item { /* Footer content */ }
    }
}

@Composable
fun PostCard(
    postPair: Pair<String, Post>,
    onLikeClick: (Pair<String, Post>) -> Unit,
    userUsername: String?
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLiked = currentUser != null && postPair.second.likes.contains(currentUser.uid)
    val likesCount = postPair.second.likes.size
    var newComment by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = postPair.second.foto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = postPair.second.titulo,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = postPair.second.fecha, color = Color.LightGray)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { onLikeClick(postPair) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like Button",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = likesCount.toString(), color = Color.Gray)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            tint = Color.Gray
                        )
                    }
                    Text(text = postPair.second.comentarios.size.toString(), color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Comentarios") },
            text = {
                Column {
                    LazyColumn {
                        items(postPair.second.comentarios) { comment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    val commentParts = comment.split(" at ")
                                    val usernameAndComment = commentParts[0].split(": ")
                                    val username = usernameAndComment[0]
                                    val commentText = usernameAndComment[1]
                                    val timestamp = commentParts[1]

                                    Text(text = username, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = commentText)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = timestamp, fontSize = 12.sp)

                                    Log.d("DEBUG", "Username del comentario: $username")
                                    Log.d("DEBUG", "DisplayName del usuario actual: $userUsername")
                                    if (currentUser != null && username == userUsername) {
                                        IconButton(onClick = {
                                            deleteComment(
                                                postPair.first,
                                                comment
                                            )
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Comment Button",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            label = { Text("Escribe un comentario") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (currentUser != null && newComment.isNotBlank()) {
                                    val fullComment = "${currentUser.displayName}: $newComment"
                                    addComment(postPair.first, fullComment)
                                    newComment = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Comment Button",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}


fun addComment(postId: String, comment: String) {
    val db = Firebase.firestore
    val postRef = db.collection("posts").document(postId)
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        val userRef = db.collection("usuarios").document(currentUser.uid)
        userRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(usuarios::class.java)
            if (user != null) {
                val username = user.username
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )
                val cleanedComment =
                    comment.trimStart(':') // Elimina cualquier dos puntos al principio del comentario
                val fullComment = "$username: $cleanedComment at $timestamp"
                postRef.update("comentarios", FieldValue.arrayUnion(fullComment))
            }
        }
    }
}

fun deleteComment(postId: String, comment: String) {
    val db = Firebase.firestore
    val postRef = db.collection("posts").document(postId)

    postRef.update("comentarios", FieldValue.arrayRemove(comment))
}

fun onLikeClick(postPair: Pair<String, Post>) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val db = Firebase.firestore
        val postRef = db.collection("posts").document(postPair.first)

        postRef.get().addOnSuccessListener { documentSnapshot ->
            val currentPost = documentSnapshot.toObject(Post::class.java)
            if (currentPost != null) {
                val likes = currentPost.likes.toMutableList()
                if (likes.contains(currentUser.uid)) {
                    // The user has already liked the post, so we remove it
                    likes.remove(currentUser.uid)
                } else {
                    // The user has not liked the post, so we add it
                    likes.add(currentUser.uid)
                }

                postRef.update("likes", likes)
            }
        }
    }
}

@Composable
fun CrearGrupoScreen(
    navController: NavController) {
    var amigosDisponibles by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var amigosSeleccionados by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var usuario by remember { mutableStateOf<usuarios?>(null) }
    var amigos by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var nombreGrupo by remember { mutableStateOf(TextFieldValue("")) }


    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = Firebase.firestore
            val userReference = db.collection("usuarios").document(userId)

            userReference.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // El documento existe, puedes obtener el objeto usuarios
                    usuario = documentSnapshot.toObject(usuarios::class.java)

                    // Obtener la lista de amigos
                    val listaAmigos = usuario?.amigos ?: emptyList()

                    if (listaAmigos.isNotEmpty()) {
                        // Obtener información de cada amigo y almacenarla en la lista 'amigos'
                        listaAmigos.forEach { amigoId ->
                            val amigoReference = db.collection("usuarios").document(amigoId)
                            amigoReference.get().addOnSuccessListener { amigoSnapshot ->
                                if (amigoSnapshot.exists()) {
                                    val amigo = amigoSnapshot.toObject(usuarios::class.java)
                                    if (amigo != null) {
                                        amigos = amigos + amigo
                                        // Actualizar la listaActual después de obtener la información del amigo
                                        amigosDisponibles = amigos
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = nombreGrupo.text,
            onValueChange = { nombreGrupo = nombreGrupo.copy(text = it) },
            label = { Text("Nombre del Grupo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        var isAmigoSeleccionado = false

        // Lista de amigos disponibles
        Text("Selecciona amigos para el grupo:")
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(amigosDisponibles) { amigo ->
                AmigoCardCrearGrupo(
                    amigo = amigo,
                    onAddClick = {
                        amigosSeleccionados = amigosSeleccionados + it
                        isAmigoSeleccionado = true
                    },
                    navController = navController,
                    isAmigoSeleccionado
                )
            }
        }


        // Lista de amigos seleccionados
        Text("Amigos seleccionados:")
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(amigosSeleccionados.distinctBy { it.id }) { amigo ->
                AmigoCardCrearGrupoSelect(
                    amigo = amigo,
                    onAddClick = {
                        amigosSeleccionados = amigosSeleccionados.filter { selectedAmigo ->
                            selectedAmigo != amigo
                        }
                    },
                    navController = navController,
                    isAmigoSeleccionado
                )
            }
        }

        // Botón para crear el grupo con amigos seleccionados
            Button(
                onClick = {
                    // Verificar que haya al menos 1 amigos seleccionados para crear un grupo
                    if (amigosSeleccionados.size >= 1 && nombreGrupo.text.isNotEmpty()) {
                        // Obtener IDs de amigos seleccionados
                        val idsAmigosSeleccionados = amigosSeleccionados.map { it.id ?: "" }
                        val idUsuarioLogeado = usuario?.id
                        val iddelGrupo = "Grupo_${UUID.randomUUID()}"

                                    // Crear un objeto Chat con la información del grupo
                                    val nuevoGrupo = Chat(
                                        id = iddelGrupo,  // Firestore generará un ID automáticamente
                                        integrantes = (listOf(idUsuarioLogeado) + idsAmigosSeleccionados).map { it!! },
                                        grupo = true,
                                        nombreGrupo = nombreGrupo.text,
                                        usuariosEnChat = null,
                                        admin = idUsuarioLogeado
                                    )

                                    // Agregar el grupo a Firestore
                                    val db = Firebase.firestore
                                    db.collection("Chat").document(iddelGrupo)
                                        .set(nuevoGrupo)
                                        .addOnSuccessListener { documentReference ->
                                            // Grupo creado exitosamente

                                            navController.navigate("mensajes")
                                        }
                                        .addOnFailureListener { e ->
                                        }
                                } else {
                                    // Mostrar un mensaje de que se necesitan al menos 2 amigos para crear un grupo
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("Crear Grupo")
                        }
                    }
                }




@Composable
fun AmigoCardCrearGrupo(
    amigo: usuarios,
    onAddClick: (usuarios) -> Unit,
    navController: NavController,
    isAmigoSeleccionado: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil del amigo a la izquierda
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        val amigoFoto = amigo.foto
                        val imageUrl = "$amigoFoto"
                        Glide.with(context)
                            .load(imageUrl)
                            .fitCenter()
                            .transform(CircleCrop())
                            .into(this)
                    }
                }, modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Green, CircleShape)
            )

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(12.dp))

            // Columna para el Usuario y Nombre del amigo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Nombre de usuario más pequeño del amigo
                Text(
                    text = amigo.username ?: "Username",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                // Nombre completo del amigo
                Text(
                    text = amigo.nombre ?: "Nombre del Amigo",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(16.dp))

            // Botón de Agregar o Quitar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                    .background(Color.Green)
                    .clickable { onAddClick(amigo) }, // Acción al hacer clic
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}

@Composable
fun AmigoCardCrearGrupoSelect(
    amigo: usuarios,
    onAddClick: (usuarios) -> Unit,
    navController: NavController,
    isAmigoSeleccionado: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil del amigo a la izquierda
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        val amigoFoto = amigo.foto
                        val imageUrl = "$amigoFoto"
                        Glide.with(context)
                            .load(imageUrl)
                            .fitCenter()
                            .transform(CircleCrop())
                            .into(this)
                    }
                }, modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Green, CircleShape)
            )

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(12.dp))

            // Columna para el Usuario y Nombre del amigo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Nombre de usuario más pequeño del amigo
                Text(
                    text = amigo.username ?: "Username",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                // Nombre completo del amigo
                Text(
                    text = amigo.nombre ?: "Nombre del Amigo",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(16.dp))

            // Botón de Agregar o Quitar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                    .background(Color.Red)
                    .clickable { onAddClick(amigo) }, // Acción al hacer clic
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Quitar",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}


@Composable
fun GrupoCard(
    grupo: Chat,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(10.dp)
            .clickable {
                // Navegar a la pantalla de detalles del grupo cuando se hace clic en el nombre
                navController.navigate("grupoChat/${grupo.id}/${grupo.admin}/${grupo.nombreGrupo}")
            },
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nombre del grupo
            Text(
                text = grupo.nombreGrupo ?: "Nombre del Grupo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 8.dp)
            )

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(16.dp))

            // Cantidad de personas en el grupo
            Text(
                text = "Miembros: ${grupo.integrantes?.size}",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileDialog(
    usuario: usuarios?,
    onCloseDialog: () -> Unit // Agrega un callback para cerrar el diálogo
) {

    var loadingState by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedImageUriState = remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            // Subir la imagen a Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

            loadingState = true // Establecer el estado de carga a true

            val uploadTask = imageRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                loadingState = false

                if (task.isSuccessful) {
                    val downloadUri = task.result

                    // Actualizar la URL de la imagen en Firestore
                    updateImageUrlInFirestore(downloadUri.toString())

                    // Actualizar el estado de la imagen seleccionada
                    selectedImageUriState.value = uri
                } else {
                    errorMessage = "Error al cargar la imagen"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onCloseDialog() },
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            val userFoto = usuario?.foto
                            val imageUrl = selectedImageUriState.value?.toString() ?: userFoto ?: ""
                            Glide.with(context)
                                .load(imageUrl)
                                .fitCenter()
                                .transform(CircleCrop())
                                .into(this)

                            setOnClickListener {
                                galleryLauncher.launch("image/*")
                            }
                        }
                    }, modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Green, CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = usuario?.username ?: "hola", fontWeight = FontWeight.Bold)
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCloseDialog() }) {
                Text("Cerrar")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.CenterVertically),
    )
}

private fun updateImageUrlInFirestore(imageUrl: String) {
    // Actualizar la URL de la imagen en Firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("usuarios").document(userId ?: "")

    userRef.update("foto", imageUrl)
        .addOnSuccessListener {
        }
        .addOnFailureListener { e ->

        }
}
