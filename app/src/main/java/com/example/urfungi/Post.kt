package com.example.urfungi

data class Post(
    var id: String = "",
    val usuario: String = "",
    val idSeta : String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val cordenadas: String = "",
    val fecha: String = "",
    val foto: String = "",
    val privacidad: String = "Privado",
    val likes: List<String> = emptyList(),
    var comentarios: List<String> = emptyList()
)