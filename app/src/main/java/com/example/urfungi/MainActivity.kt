package com.example.urfungi

import MapScreen
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.urfungi.Curiosidades.SetasListScreen
import com.example.urfungi.QuizJuego.QuizScreenFromFirebase
import com.example.urfungi.Recetas.RecetasSetasListScreen
import com.example.urfungi.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

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
                                            if (!destinoSeleccionado) {
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
                                    MensajesScreen()
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
                                    MapScreen()
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
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(paddingValues),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = stringResource(id = Destino.Destino3.nombre))
                                            }
                                        }
                                    }
                                }

                                composable("setas_list_item") {
                                    SetasListScreen()
                                }

                                composable("recetas_list_item") {
                                    RecetasSetasListScreen()
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
                                        PostsScreen()
                                    }
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
                                        QuizScreenFromFirebase()
                                    }
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
    fun MensajesScreen() {
        var usuario by remember { mutableStateOf<usuarios?>(null) }
        var amigos by remember { mutableStateOf<List<usuarios>>(emptyList()) }


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

                        // Obtener información de cada amigo y almacenarla en la lista 'amigos'
                        listaAmigos.forEach { amigoId ->
                            val amigoReference = db.collection("usuarios").document(amigoId)
                            amigoReference.get().addOnSuccessListener { amigoSnapshot ->
                                if (amigoSnapshot.exists()) {
                                    val amigo = amigoSnapshot.toObject(usuarios::class.java)
                                    if (amigo != null) {
                                        amigos = amigos + amigo
                                    }
                                }
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
                    // Manejar errores aquí
                    Log.e(TAG, "Error al obtener datos del usuario en Firestore: $e")
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
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

            // Tarjeta de información del usuario
            if (usuario != null) {
                MensajesCard(
                    usuario = usuario,
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        navigateToLoginAppActivity()
                    },
                    onEditClick = {}
                )
            } else {
                Text("El usuario es nulo")
            }

            // Texto "Amigos"
            Text(
                text = "Amigos:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .offset(y = 200.dp) // Ajustar según sea necesario para evitar superposición
            )

            // Botón "Añadir Amigos"
            Button(
                onClick = {
                    // Implementa la lógica para añadir amigos
                },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .offset(y = 240.dp) // Ajustar según sea necesario para evitar superposición
            ) {
                Text("Añadir Amigos")
            }

            // Lista de tarjetas de amigos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 310.dp) // Ajustar según sea necesario para evitar superposición
            ) {
                items(amigos) { amigo ->
                    AmigoCard(amigo = amigo)
                }
            }
        }
    }


    @Composable
    fun AmigoCard(amigo: usuarios) {
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
                AndroidView(factory = { context ->
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
                        modifier = Modifier.padding(top = 8.dp) // Padding superior al username
                    )

                    // Nombre completo del amigo
                    Text(
                        text = amigo.nombre ?: "Nombre del Amigo",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    @Composable
    fun MensajesCard(usuario: usuarios?, onLogoutClick: () -> Unit, onEditClick: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            usuario?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),

                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),

                    verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen de perfil del usuario a la izquierda
                        AndroidView(factory = { context ->
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
                            .size(80.dp)
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
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Nombre de usuario más pequeño
                            Text(
                                text = usuario.username ?: "Username",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp) // Padding superior al username
                            )

                            // Nombre completo del usuario
                            Text(
                                text = usuario.nombre ?: "Nombre del Usuario",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // Espaciador horizontal
                        Spacer(modifier = Modifier.width(16.dp))

                        // Botones a la derecha del todo
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                        ) {
                            // Botón de Configuración
                            IconButton(
                                onClick = { onEditClick() },
                                modifier = Modifier
                                    .size(40.dp) // Aumentado el tamaño del botón
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                                    .padding(top = 8.dp, end = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)

                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón de Logout
                            IconButton(
                                onClick = { onLogoutClick() },
                                modifier = Modifier
                                    .size(40.dp) // Aumentado el tamaño del botón
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .padding(top = 8.dp, end = 8.dp)

                            ) {
                                Icon(imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
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
