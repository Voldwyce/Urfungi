package com.example.urfungi

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class DestinoJuego(
    val ruta: String,
    @StringRes val nombre: Int,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
) {
    Destinojuego(
        ruta = "destinojuego",
        nombre = R.string.destinojuego,
        icono = Icons.Outlined.Search,
        iconoSeleccionado = Icons.Filled.Search
    )
}