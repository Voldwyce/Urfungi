package com.example.urfungi

import MapScreen
import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.urfungi.ui.theme.AppTheme
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.urfungi.Curiosidades.SetasListItem
import com.example.urfungi.Curiosidades.SetasListScreen
import com.example.urfungi.Curiosidades.setas
import com.example.urfungi.Recetas.RecipeListItem
import com.example.urfungi.Recetas.recipes
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.w3c.dom.Text
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

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
                                                        IconButton(onClick = { /* Handle navigation icon press */ }) {
                                                            Icon(
                                                                Icons.Filled.PlayArrow,
                                                                contentDescription = "Navigation Icon"
                                                            )
                                                        }
                                                    },
                                                    actions = {
                                                        IconButton(onClick = { /* Handle send message icon press */ }) {
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
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LazyColumn {
                                            items(recipes) { recipe ->
                                                RecipeListItem(recipe = recipe)
                                            }
                                        }
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
                                        SetasListScreen()
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
            }.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
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

}
