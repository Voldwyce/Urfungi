package com.example.urfungi.QuizJuego

import com.example.urfungi.R


data class Question(
    val imagenResId: Int,
    val answers: List<String>,
    val correctAnswer: String
)

val questions = listOf(
    Question(
        R.drawable.teemo,
        answers = listOf(
            "Teemo",
            "Boletus edulis",
            "Lactarius deliciosus",
            "Agaricus bisporus",
            "Cantharellus cibarius"
        ),
        correctAnswer = "Teemo"
    ),
    Question(
        R.drawable.champ,
        answers = listOf(
            "Amanita muscaria",
            "Boletus edulis",
            "Lactarius deliciosus",
            "Agaricus bisporus",
            "Cantharellus cibarius"
        ),
        correctAnswer = "Boletus edulis"
    ),
    Question(
        R.drawable.teemo,
        answers = listOf(
            "Amanita muscaria",
            "Boletus edulis",
            "Lactarius deliciosus",
            "Agaricus bisporus",
            "Cantharellus cibarius"
        ),
        correctAnswer = "Lactarius deliciosus"
    ),
    Question(
        R.drawable.teemo,
        answers = listOf(
            "Amanita muscaria",
            "Boletus edulis",
            "Lactarius deliciosus",
            "Agaricus bisporus",
            "Cantharellus cibarius"
        ),
        correctAnswer = "Agaricus bisporus"
    ),
    Question(
        R.drawable.teemo,
        answers = listOf(
            "Amanita muscaria",
            "Boletus edulis",
            "Lactarius deliciosus",
            "Agaricus bisporus",
            "Cantharellus cibarius"
        ),
        correctAnswer = "Cantharellus cibarius"
    )
)
