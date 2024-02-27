package com.example.urfungi.Usuarios

data class Chat(
    val id: String? = "",
    val integrantes: List<String>? = null,
    val grupo: Boolean? = false,
    val nombreGrupo: String? = "",
    val usuariosEnChat: String? = ""
)