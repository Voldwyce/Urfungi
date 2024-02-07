package com.example.urfungi

data class Post(
    val usuario: String = "",
    val idSeta : String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val cordenadas: String = "",
    val fecha: String = "",
    val foto: String = "",
    val likes: List<String> = emptyList()
)