package com.example.urfungi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.min


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

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

                                        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    }
                                },
                                exitTransition = {
                                    when (targetState.destination.route) {
                                        Destino.Destino3.ruta -> slideOutOfContainer(
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
                                    Text(text = stringResource(id = Destino.Destino4.nombre))
                                }
                            }

                            composable(
                                route = Destino.Destino5.ruta,
                                enterTransition = {
                                    when (initialState.destination.route) {
                                        Destino.Destino4.ruta -> slideIntoContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Left
                                        )

                                        else -> slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                    }
                                },
                                exitTransition = {
                                    when (targetState.destination.route) {
                                        Destino.Destino4.ruta -> slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Right
                                        )

                                        else -> slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 16.dp, top = 40.dp, bottom = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LazyColumn {
                                        items(setas) { seta ->
                                            SetasListItem(seta = seta) {
                                                println("Descripción completa: ${seta.descripcion}")
                                                println("Nombre cientifico:  ${seta.nombrecientifico}")
                                                println("Estación: ${seta.estacion}")
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
    }

    data class Seta(
        val nombre: String, /*val imagenResId: Int,*/
        val descripcion: String,
        val nombrecientifico: String,
        val estacion: String
    )

    val setas = listOf(
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),
        Seta(
            "Champiñones", /*R.drawable.seta1*/
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
            "Agaricus bisporus",
            "Otoño"
        ),

        )

    @Composable
    fun SetasListItem(seta: Seta, onItemClick: () -> Unit) {
        var isExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 15.dp, 30.dp)
                .clickable { onItemClick(); isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Image(
                    painter = painterResource(id = seta.imagenResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 16.dp)
                )*/
                Column {
                    Text(text = seta.nombre, fontWeight = FontWeight.Bold)
                    // Lógica para mostrar solo una parte del texto si no está expandido
                    if (!isExpanded) {
                        Text(text = seta.descripcion.substring(0, min(14, seta.descripcion.length)))
                    } else {
                        // Si está expandido, muestra todo el texto
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = seta.descripcion, textAlign = TextAlign.Justify)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nombre cientifico",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = seta.nombrecientifico)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Estación", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = seta.estacion)
                    }
                }
            }
        }
    }

}