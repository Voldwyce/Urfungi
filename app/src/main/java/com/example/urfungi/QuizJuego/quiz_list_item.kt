package com.example.urfungi.QuizJuego

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urfungi.R
import com.example.urfungi.Setas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


@Composable
fun QuizScreenFromFirebase() {
    var setas by remember { mutableStateOf<List<Setas>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var currentSetaName by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        try {
            val querySnapshot = firestore.collection("setas").get().await()
            val list = mutableListOf<Setas>()
            for (document in querySnapshot.documents) {
                val seta = document.toObject(Setas::class.java)
                if (seta != null) {
                    list.add(seta)
                }
            }
            setas = list.shuffled() // Se mezclan las setas obtenidas aleatoriamente
        } catch (e: Exception) {
            e.printStackTrace()
            setas = emptyList()
        }
    }

    if (isGameOver) {
        GameOverScreen(
            score = currentQuestionIndex + 1,
            onRestartClicked = {
                currentQuestionIndex = 0
                isGameOver = false
            }
        )
    } else {
        if (setas.isNotEmpty() && currentQuestionIndex < setas.size) {
            val currentSeta = setas[currentQuestionIndex]
            currentSetaName = currentSeta.Nombre

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quiz Game",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = currentSeta.Imagen,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = currentSeta.Nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        val opcionesRespuesta = setas.shuffled().take(6).toMutableList()
                        if (!opcionesRespuesta.contains(currentSeta)) {
                            opcionesRespuesta[Random.nextInt(4)] = currentSeta
                        }
                        opcionesRespuesta.forEach { answer ->
                            Button(
                                onClick = {
                                    if (answer.Nombre == currentSetaName) {
                                        if (currentQuestionIndex == setas.lastIndex) {
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
                                Text(text = answer.Nombre)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(score: Int, onRestartClicked: () -> Unit) {
    // Pantalla de juego terminado
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de juego terminado
        Text(
            text = "¡Juego terminado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Mostrar puntuación
        Text(
            text = "Tu puntuación es $score/22",
            fontSize = 18.sp,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(13.dp))

        // Mensaje basado en la puntuación
        Text(
            text = when {
                score == 22 -> "¡Excelente! Sabes mucho sobre el tema."
                score in 16..19 -> "¡Ya casi lo tienes!"
                score in 11..15 -> "¡Vas por buen camino!"
                score in 1..10 -> "¡Mmmm! Malardo."
                else -> "¡0 correctas XD! Puedes hacerlo mejor."
            },
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para reiniciar el juego
        Button(
            onClick = { onRestartClicked() },
            modifier = Modifier
                .fillMaxWidth(0.5f)
        ) {
            Text(text = "Volver a jugar")
        }
    }
}

