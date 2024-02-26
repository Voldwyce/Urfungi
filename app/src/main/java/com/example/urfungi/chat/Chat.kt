package com.example.urfungi.chat

data class Chat(
    val id: String? = "",
    val integrantes: List<String>? = null,
    val grupo: Boolean? = false,
    val nombreGrupo: String? = "",
    val usuariosEnChat: String? = ""
)