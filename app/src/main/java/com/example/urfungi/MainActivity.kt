package com.example.urfungi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.urfungi.Curiosidades.SetasListScreen
import com.example.urfungi.QuizJuego.Question
import com.example.urfungi.QuizJuego.questions
import com.example.urfungi.Recetas.RecetasSetasListScreen

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
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 16.dp, top = 40.dp, bottom = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RecetasSetasListScreen()
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
                                    SetasListScreen()
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
                                    QuizScreen(questions = questions)

                                }
                            }
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun QuizScreen(questions: List<Question>) {
        var currentQuestionIndex by remember { mutableStateOf(0) }
        var isGameOver by remember { mutableStateOf(false) }

        if (isGameOver) {
            GameOverScreen(
                score = currentQuestionIndex + 1,
                onRestartClicked = {
                    currentQuestionIndex = 0
                    isGameOver = false
                }
            )
        } else {
            val currentQuestion = questions[currentQuestionIndex]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quiz Game",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    {
                        Image(
                            painter = painterResource(id = currentQuestion.imagenResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomEnd = 0.dp,
                                        bottomStart = 0.dp
                                    )
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        currentQuestion.answers.forEach { answer ->
                            Button(
                                onClick = {
                                    if (answer == currentQuestion.correctAnswer) {
                                        if (currentQuestionIndex == questions.lastIndex) {
                                            isGameOver = true
                                        } else {
                                            currentQuestionIndex++
                                        }
                                    } else {
                                        isGameOver = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(text = answer)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun GameOverScreen(score: Int, onRestartClicked: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Juego terminado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Tu puntuación es $score/5",
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = when {
                    score == 5 -> "¡Excelente! Sabes mucho sobre el tema."
                    score >= 3 -> "¡Buen trabajo! Sigues mejorando."
                    else -> "¡Sigue practicando! Puedes hacerlo mejor."
                },
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onRestartClicked() },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
            ) {
                Text(text = "Volver a jugar")
            }
        }
    }


}