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
import com.example.urfungi.R
import com.example.urfungi.Setas
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


@Composable
fun QuizScreenFromFirebase() {
    // Estado para almacenar la lista de setas obtenida de Firebase
    var setas by remember { mutableStateOf<List<Setas>>(emptyList()) }
    // Estado para almacenar el índice de la pregunta actual
    var currentQuestionIndex by remember { mutableStateOf(0) }
    // Estado para controlar si el juego ha terminado
    var isGameOver by remember { mutableStateOf(false) }
    // Variable para almacenar el nombre de la seta actual
    var currentSetaName by remember { mutableStateOf("") }

    // Instancia de Firebase Firestore
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        try {
            // Obtención de datos de Firebase y actualización del estado
            val querySnapshot = firestore.collection("setas").get().await()
            val list = mutableListOf<Setas>()
            for (document in querySnapshot.documents) {
                val seta = document.toObject(Setas::class.java)
                if (seta != null) {
                    list.add(seta)
                }
            }
            setas = list
        } catch (e: Exception) {
            // Manejo de errores
            e.printStackTrace()
            setas = emptyList()
        }
    }

    // Pantalla de juego terminado
    if (isGameOver) {
        GameOverScreen(
            score = currentQuestionIndex + 1, // +1 porque el índice comienza desde 0
            onRestartClicked = {
                currentQuestionIndex = 0
                isGameOver = false
            }
        )
    } else {
        // Pantalla de juego en curso
        if (setas.isNotEmpty() && currentQuestionIndex < setas.size) {
            val currentSeta = setas[currentQuestionIndex]
            currentSetaName = currentSeta.Nombre // Guardar el nombre de la seta actual

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Título del juego
                Text(
                    text = "Quiz Game",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )

                // Carta de pregunta y opciones de respuesta
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mostrar imagen de la seta actual
                        // (Aquí asumo que tienes un campo en la clase Setas llamado "Imagen"
                        // que contiene la URL de la imagen. Debes ajustarlo según tu estructura de datos.)
                        Image(
                            painter = painterResource(id = R.drawable.teemo), // Reemplaza con la URL de la imagen
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Mostrar el nombre de la seta actual
                        Text(
                            text = currentSeta.Nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Mostrar opciones de respuesta (setas aleatorias)
                        val opcionesRespuesta = setas.shuffled().take(6).toMutableList()
                        if (!opcionesRespuesta.contains(currentSeta)) {
                            // Asegurarse de que el nombre de la seta actual esté presente entre las opciones de respuesta
                            opcionesRespuesta[Random.nextInt(4)] = currentSeta
                        }
                        opcionesRespuesta.forEach { answer ->
                            Button(
                                onClick = {
                                    if (answer.Nombre == currentSetaName) { // Verificar la respuesta correcta usando el nombre de la seta actual
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

