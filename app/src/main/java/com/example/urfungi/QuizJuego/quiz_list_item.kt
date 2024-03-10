package com.example.urfungi.QuizJuego

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.example.urfungi.Repo.Setas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import com.example.urfungi.Repo.Setas
import java.util.TimeZone

enum class DifficultyLevel {
    EASY, INTERMEDIATE, HARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreenFromFirebase(onHighscoresClicked: () -> Unit, onQuizPostsClicked: () -> Unit) {
    var difficultyLevel by remember { mutableStateOf<DifficultyLevel?>(null) }

    if (difficultyLevel == null) {
        ChooseDifficultyDialog { selectedDifficulty ->
            difficultyLevel = selectedDifficulty
        }
    } else {
        var setas by remember { mutableStateOf<List<Setas>>(emptyList()) }
        var currentQuestionIndex by remember { mutableStateOf(0) }
        var isGameOver by remember { mutableStateOf(false) }
        var currentSetaName by remember { mutableStateOf("") }
        var userRecord by remember { mutableStateOf(0) }
        var score by remember { mutableStateOf(0) }
        var remainingTime by remember { mutableStateOf(30) }
        var quizRecordSaved by remember { mutableStateOf(false) }
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        var timer: CountDownTimer? by remember { mutableStateOf(null) }

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

        fun restartGame() {
            currentQuestionIndex = 0
            isGameOver = false
            score = 0
            remainingTime = 30
            initializeTimer()
            quizRecordSaved = false
        }

        DisposableEffect(Unit) {
            initializeTimer()

            onDispose {
                timer?.cancel()
            }
        }

        remainingTime = rememberUpdatedState(newValue = remainingTime).value

        LaunchedEffect(Unit) {
            try {
                val querySnapshot = firestore.collection("setas").get().await()
                val list = mutableListOf<Setas>()
                for (document in querySnapshot.documents) {
                    val seta = document.toObject<Setas>()
                    if (seta != null) {
                        list.add(seta)
                    }
                }

                val groupedSetas = list.groupBy { it.Dificultad }
                val shuffledSetas = groupedSetas.mapValues { (_, setas) -> setas.shuffled() }
                setas = shuffledSetas.toSortedMap().values.flatten()

                val userDocument =
                    firestore.collection("usuarios").document(auth.currentUser?.uid ?: "").get()
                        .await()
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

        fun saveQuizRecord(score: Int) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val currentDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)

                firestore.collection("RegistrosQuiz")
                    .whereEqualTo("fecha", formattedDate)
                    .whereEqualTo("idusuario", currentUser.uid)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result.documents
                            if (documents.isNotEmpty()) {
                                val existingRecord = documents[0]
                                val existingScore = existingRecord.get("score") as? Long ?: 0
                                val updatedScore = existingScore + score

                                existingRecord.reference.update("score", updatedScore)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "QuizRecord",
                                            "Registro del quiz actualizado con ID: ${existingRecord.id}"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "QuizRecord",
                                            "Error al actualizar el registro del quiz",
                                            e
                                        )
                                    }
                            } else {
                                val quizRecord = hashMapOf(
                                    "fecha" to formattedDate,
                                    "idusuario" to currentUser.uid,
                                    "score" to score
                                )

                                firestore.collection("RegistrosQuiz")
                                    .add(quizRecord)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(
                                            "QuizRecord",
                                            "Registro del quiz guardado con ID: ${documentReference.id}"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "QuizRecord",
                                            "Error al guardar el registro del quiz",
                                            e
                                        )
                                    }
                            }
                        } else {
                            Log.w(
                                "QuizRecord",
                                "Error al obtener registros del quiz",
                                task.exception
                            )
                        }
                    }
            }
        }

        // Definición del número de respuestas basado en la dificultad seleccionada
        val numberOfAnswers = when (difficultyLevel) {
            DifficultyLevel.EASY -> 1
            DifficultyLevel.INTERMEDIATE -> 3
            DifficultyLevel.HARD -> 4
            else -> 3 // Valor predeterminado para cualquier otra dificultad o si no se ha seleccionado ninguna
        }

        remainingTime = rememberUpdatedState(newValue = remainingTime).value

        if (isGameOver) {
            val finalScore = score
            if (finalScore > userRecord) {
                firestore.collection("usuarios").document(auth.currentUser?.uid ?: "")
                    .update("record", finalScore)
                    .addOnSuccessListener {
                        userRecord = finalScore
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }

            GameOverScreen(
                score = finalScore,
                onRestartClicked = { restartGame() },
                onShuffleSetas = { setas = setas.shuffled() },
                onPublishClicked = { quizPost ->
                    saveQuizPost(quizPost)
                }
            )

            if (!quizRecordSaved) {
                saveQuizRecord(score)
            }
            quizRecordSaved = true
        } else {
            if (setas.isNotEmpty() && currentQuestionIndex < setas.size) {
                val currentSeta = setas[currentQuestionIndex]
                currentSetaName = currentSeta.Nombre

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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

                        IconButton(
                            onClick = { onQuizPostsClicked() } // Navega a la pantalla de publicaciones del quiz al hacer clic en el ícono
                        ) {
                            Icon(Icons.Filled.PostAdd, contentDescription = "quizPosts")
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
                            val opcionesRespuesta =
                                remember { mutableStateOf<List<Setas>>(emptyList()) }
                            if (opcionesRespuesta.value.isEmpty()) {
                                val opcionesDistintas = mutableListOf(currentSeta)
                                opcionesDistintas.addAll(setas.filterNot {
                                    it.Nombre.replace(
                                        " ",
                                        ""
                                    ) == currentSeta.Nombre.replace(" ", "")
                                }.shuffled().take(numberOfAnswers))
                                opcionesRespuesta.value = opcionesDistintas.shuffled()
                                timer?.start()
                            }
                            opcionesRespuesta.value.forEachIndexed { index, answer ->
                                Button(
                                    onClick = {
                                        if (answer.Nombre.replace(
                                                " ",
                                                ""
                                            ) == currentSetaName.replace(" ", "")
                                        ) {
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
}

fun saveQuizPost(quizPost: QuizPost) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("QuizPosts")
        .add(quizPost)
        .addOnSuccessListener { documentReference ->
            Log.d("QuizPost", "Quiz post guardado con ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("QuizPost", "Error al guardar el post del quiz", e)
        }
}

@Composable
fun QuizPostScreen() {
    var quizPosts by remember { mutableStateOf<List<QuizPost>>(emptyList()) }

    LaunchedEffect(Unit) {
        quizPosts = getQuizPostsFromDatabase()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Publicaciones de Quiz 🥵",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 50.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(quizPosts) { quizPost ->
                QuizPostItem(quizPost = quizPost)
                Spacer(modifier = Modifier.height(5.dp)) // Agregar espacio entre las publicaciones
            }
        }
    }
}
suspend fun getQuizPostsFromDatabase(): List<QuizPost> {
    val firestore = FirebaseFirestore.getInstance()
    val quizPosts = mutableListOf<QuizPost>()
    try {
        val querySnapshot = firestore.collection("QuizPosts").get().await()
        for (document in querySnapshot.documents) {
            val userId = document.getString("userId") ?: ""
            val score = document.getLong("score")?.toInt() ?: 0
            val comment = document.getString("comment") ?: ""
            val quizPost = QuizPost(userId = userId, score = score, comment = comment)
            quizPosts.add(quizPost)
        }
    } catch (e: Exception) {
        Log.e("getQuizPostsFromDatabase", "Error getting quiz posts", e)
    }
    return quizPosts
}

@Composable
fun QuizPostItem(quizPost: QuizPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp) // Redondear las esquinas del card
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val userName = remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                userName.value = getUserName(quizPost.userId)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Usuario: ${userName.value}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Puntuación: ${quizPost.score}",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Comentario: ${quizPost.comment}",
                fontSize = 14.sp
            )
        }
    }
}


suspend fun getUserName(userId: String): String {
    val firestore = FirebaseFirestore.getInstance()
    var userName = ""
    try {
        val userDocument = firestore.collection("usuarios").document(userId).get().await()
        userName = userDocument.getString("username") ?: ""
    } catch (e: Exception) {
        Log.e("getUserName", "Error getting user name", e)
    }
    return userName
}

suspend fun getUserName2(idusuario: String): String {
    val firestore = FirebaseFirestore.getInstance()
    var userName = ""
    try {
        val userDocument = firestore.collection("usuarios").document(idusuario).get().await()
        userName = userDocument.getString("username") ?: ""
    } catch (e: Exception) {
        Log.e("getUserName2", "Error getting user name", e)
    }
    return userName
}
@Composable
fun ChooseDifficultyDialog(onDifficultySelected: (DifficultyLevel) -> Unit) {
    val dialogDismissed = remember { mutableStateOf(false) }

    if (!dialogDismissed.value) {
        AlertDialog(
            onDismissRequest = {
                // No hacer nada al presionar afuera del diálogo
            },
            title = { Text("Selecciona un nivel de dificultad") },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 80.dp)
                ) {
                    Button(
                        onClick = {
                            onDifficultySelected(DifficultyLevel.EASY)
                            dialogDismissed.value = true
                        },
                        modifier = Modifier.fillMaxWidth() // Hacer que el botón ocupe todo el ancho disponible
                    ) {
                        Text("Fácil")
                    }
                    Button(
                        onClick = {
                            onDifficultySelected(DifficultyLevel.INTERMEDIATE)
                            dialogDismissed.value = true
                        },
                        modifier = Modifier.fillMaxWidth() // Hacer que el botón ocupe todo el ancho disponible
                    ) {
                        Text("Intermedio")
                    }
                    Button(
                        onClick = {
                            onDifficultySelected(DifficultyLevel.HARD)
                            dialogDismissed.value = true
                        },
                        modifier = Modifier.fillMaxWidth() // Hacer que el botón ocupe todo el ancho disponible
                    ) {
                        Text("Difícil")
                    }
                }
            }
        )
    }
}


@Composable
fun GameOverScreen(
    score: Int,
    onRestartClicked: () -> Unit,
    onShuffleSetas: () -> Unit,
    onPublishClicked: (QuizPost) -> Unit
) {

    var comment by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }


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

        // Botón para publicar puntuación
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Publicar puntuación")
        }

        // Cuadro de diálogo para ingresar el comentario
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Publicar puntuación") },
                text = {
                    Column {
                        Text("Ingresa tu comentario:")
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Crear el objeto QuizPost y llamar a la función de callback
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                val quizPost = QuizPost(
                                    userId = currentUser.uid,
                                    score = score,
                                    comment = comment
                                )
                                onPublishClicked(quizPost)
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Publicar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
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

enum class HighscoreFilter {
    CURRENT_HIGHSCORE,
    GLOBAL_TOP_10,
    TODAY_TOP
}

suspend fun getTopRecordsFromDatabase(
    limit: Int,
    filterType: HighscoreFilter
): List<Pair<String, Int>> {
    val firestore = FirebaseFirestore.getInstance()
    val recordsList = mutableListOf<Pair<String, Int>>()
    try {
        when (filterType) {
            HighscoreFilter.CURRENT_HIGHSCORE, HighscoreFilter.GLOBAL_TOP_10 -> {
                // Consulta la colección de "usuarios"
                val query = firestore.collection("usuarios")
                    .orderBy("record", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                val querySnapshot = query.get().await()
                for (document in querySnapshot.documents) {
                    val username = document.getString("username") ?: "Anonymous"
                    val record = document.getLong("record")?.toInt() ?: 0
                    recordsList.add(username to record)
                }
            }

            HighscoreFilter.TODAY_TOP -> {
                // Obtener la fecha actual en formato de cadena "yyyy-MM-dd"
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Consultar la colección de "RegistrosQuiz" para obtener los registros del día actual
                val querySnapshot = firestore.collection("RegistrosQuiz")
                    .whereEqualTo("fecha", currentDate) // Filtrar por la fecha actual
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()
                for (document in querySnapshot.documents) {
                    val idUsuario = document.getString("idusuario") ?: ""
                    val username = getUserName2(idUsuario)
                    val score = document.getLong("score")?.toInt() ?: 0
                    recordsList.add(username to score)
                }
            }

        }
    } catch (e: Exception) {
        Log.e("getTopRecordsFromDatabase", "Error getting top records", e)
    }
    return recordsList
}


@Composable
fun HighscoresScreen() {
    var filterType by remember { mutableStateOf(HighscoreFilter.CURRENT_HIGHSCORE) }
    var recordsList by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(filterType) {
        recordsList = getTopRecordsFromDatabase(10, filterType)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Botón para Mejores 10 Globalmente
            Button(
                onClick = { filterType = HighscoreFilter.GLOBAL_TOP_10 },
                colors = ButtonDefaults.buttonColors(
                    contentColor = if (filterType == HighscoreFilter.GLOBAL_TOP_10) Color.White else Color.Black
                )
            ) {
                Text("Mejores 10 Globalmente")
            }

            // Botón para Mejores del Día
            Button(
                onClick = { filterType = HighscoreFilter.TODAY_TOP },
                colors = ButtonDefaults.buttonColors(
                    contentColor = if (filterType == HighscoreFilter.TODAY_TOP) Color.White else Color.Black
                )
            ) {
                Text("Mejores del Día")
            }
        }

        RecordsList(recordsList)
    }
}



