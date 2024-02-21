package com.example.urfungi.QuizJuego

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urfungi.Setas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreenFromFirebase(onHighscoresClicked: () -> Unit) {
    // Declaración de variables de estado para el juego
    var setas by remember { mutableStateOf<List<Setas>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var currentSetaName by remember { mutableStateOf("") }
    var userRecord by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var remainingTime by remember { mutableStateOf(30) }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var timer: CountDownTimer? by remember { mutableStateOf(null) }

    // Función para inicializar el temporizador
    fun initializeTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(remainingTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                isGameOver = true
            }
        }
    }

    // Método para reiniciar el juego
    fun restartGame() {
        currentQuestionIndex = 0
        isGameOver = false
        score = 0
        remainingTime = 30 // Reiniciar el tiempo a 30 segundos
        initializeTimer() // Iniciar el temporizador nuevamente
    }

    DisposableEffect(Unit) {
        initializeTimer()

        onDispose {
            timer?.cancel()
        }
    }

    // Declaración de variable de estado para el tiempo restante
    remainingTime = rememberUpdatedState(newValue = remainingTime).value

    // Inicialización de datos
    LaunchedEffect(Unit) {
        try {
            // Obtener setas desde Firestore
            val querySnapshot = firestore.collection("setas").get().await()
            val list = mutableListOf<Setas>()
            for (document in querySnapshot.documents) {
                val seta = document.toObject(Setas::class.java)
                if (seta != null) {
                    list.add(seta)
                }
            }

            val groupedSetas = list.groupBy { it.Dificultad }
            val shuffledSetas = groupedSetas.mapValues { (_, setas) -> setas.shuffled() }
            setas = shuffledSetas.toSortedMap().values.flatten()

            // Obtener el récord del usuario desde Firestore
            val userDocument = firestore.collection("usuarios").document(auth.currentUser?.uid ?: "").get().await()
            val recordFromDatabase = userDocument.get("record") as? Long
            if (recordFromDatabase != null) {
                userRecord = recordFromDatabase.toInt()
                score = 0
            }

        } catch (e: Exception) {
            e.printStackTrace()
            setas = emptyList()
        }
    }

    // Lógica del juego
    if (isGameOver) {
        // Manejar el juego terminado
        val finalScore = score
        // Actualizar récord en Firestore si es necesario
        if (finalScore > userRecord) {
            firestore.collection("usuarios").document(auth.currentUser?.uid ?: "").update("record", finalScore)
                .addOnSuccessListener {
                    userRecord = finalScore
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
        // Mostrar pantalla de juego terminado
        GameOverScreen(
            score = finalScore,
            onRestartClicked = {
                restartGame() // Reiniciar el juego
            },
            onShuffleSetas = {
                setas = setas.shuffled()
            }
        )
    } else {
        // Lógica del juego mientras no haya terminado
        if (setas.isNotEmpty() && currentQuestionIndex < setas.size) {
            val currentSeta = setas[currentQuestionIndex]
            currentSetaName = currentSeta.Nombre

            // Mostrar pregunta actual y opciones de respuesta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Mostrar información del juego (puntuación, tiempo, etc.)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Record: $userRecord",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(10.dp)
                    )

                    IconButton(
                        onClick = { onHighscoresClicked() }
                    ) {
                        Icon(Icons.Filled.List, contentDescription = "Highscores")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Puntuación: $score",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(10.dp)
                    )

                    Text(text = "Tiempo: $remainingTime")
                }

                // Mostrar la imagen de la seta actual
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

                        // Mostrar opciones de respuesta
                        val opcionesRespuesta = remember { mutableStateOf<List<Setas>>(emptyList()) }
                        if (opcionesRespuesta.value.isEmpty()) {
                            val opcionesDistintas = mutableListOf(currentSeta)
                            opcionesDistintas.addAll(setas.filterNot { it.Nombre.replace(" ", "") == currentSeta.Nombre.replace(" ", "") }.shuffled().take(3))
                            opcionesRespuesta.value = opcionesDistintas.shuffled()
                            timer?.start()
                        }
                        opcionesRespuesta.value.forEachIndexed { index, answer ->
                            Button(
                                onClick = {
                                    if (answer.Nombre.replace(" ", "") == currentSetaName.replace(" ", "")) {
                                        // Calcular la puntuación basada en el tiempo restante
                                        val points = (remainingTime * 100) / 30
                                        score += points
                                    } else {
                                        isGameOver = true
                                        timer?.cancel()
                                        timer = null
                                    }
                                    currentQuestionIndex++
                                    opcionesRespuesta.value = emptyList()
                                    remainingTime = 30
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
fun GameOverScreen(score: Int, onRestartClicked: () -> Unit, onShuffleSetas: () -> Unit) {
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
            text = "Tu puntuación es $score",
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

        Button(
            onClick = {
                onRestartClicked()
                onShuffleSetas() // Barajar las setas al reiniciar
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
        ) {
            Text(text = "Volver a jugar")
        }
    }
}

@Composable
fun RecordsList(recordsList: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Top 10",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        recordsList.forEachIndexed { index, record ->
            Text(
                text = "${index + 1}. ${record.first}: ${record.second}",
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

suspend fun getTopRecordsFromDatabase(limit: Int): List<Pair<String, Int>> {
    val firestore = FirebaseFirestore.getInstance()
    val recordsList = mutableListOf<Pair<String, Int>>()
    try {
        val querySnapshot = firestore.collection("usuarios")
            .orderBy("record", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        for (document in querySnapshot.documents) {
            val username = document.getString("username") ?: "Anonymous"
            val record = document.getLong("record")?.toInt() ?: 0
            recordsList.add(username to record)
        }
    } catch (e: Exception) {
        Log.e("getTopRecordsFromDatabase", "Error getting top records", e)
    }
    return recordsList
}

@Composable
fun HighscoresScreen() {
    var recordsList by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(Unit) {
        recordsList = getTopRecordsFromDatabase(10)
    }

    RecordsList(recordsList)
}

