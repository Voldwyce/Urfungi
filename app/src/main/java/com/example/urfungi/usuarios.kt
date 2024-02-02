package com.example.urfungi

data class usuarios(
    val id: String = "",
    val nombre: String? = null,
    val email: String? = null,
    val username: String? = null,
    val foto: String? = null,
    val fechaNacimiento: String? = null,
    val totalAciertos: Int = 0,
    val totalFallos: Int = 0
)

