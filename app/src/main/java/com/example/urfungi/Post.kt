package com.example.urfungi

data class Post(
    val Usuario: String = "",
    val idSeta : String = "",
    val Titulo: String = "",
    val Descripcion: String = "",
    val Cordenadas: String = "",
    val Fecha: String = "",
    val Foto: String = "",
    val Likes: List<String> = emptyList()
)