package com.example.urfungi

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val username: String = "",
    val foto: String = "",
    val fechaNacimiento: String = "",
    val totalAciertos: Int = 0,
    val totalFallos: Int = 0
)
