package com.example.urfungi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.urfungi.Curiosidades.SetasListItem
import com.example.urfungi.Curiosidades.SetasListScreen
import com.example.urfungi.Curiosidades.setas
import com.example.urfungi.Recetas.RecipeListItem
import com.example.urfungi.Recetas.recipes
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

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
                                startDestination = Destino.entries.first().ruta
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
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stringResource(id = Destino.Destino1.nombre))
                                    }
                                }

                                composable(
                                    route = Destino.Destino2.ruta,
                                    enterTransition = {
                                        when (initialState.destination.route) {
                                            Destino.Destino1.ruta -> slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Left
                                            )

                                            else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                        }
                                    },
                                    exitTransition = {
                                        when (targetState.destination.route) {
                                            Destino.Destino1.ruta -> slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Right
                                            )

                                            else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                        }

                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stringResource(id = Destino.Destino2.nombre))
                                    }
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
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stringResource(id = Destino.Destino3.nombre))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
