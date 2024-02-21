package com.example.urfungi

data class Chat(
    val id: String? = "",
    val integrantes: List<String>? = null,
    val grupo: Boolean? = false,
    val nombreGrupo: String? = ""
)