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