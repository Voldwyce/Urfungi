package com.example.urfungi

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destino(
    val ruta: String,
    @StringRes val nombre: Int,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
) {
    Destino1(
        ruta = "destino1",
        nombre = R.string.destino1,
        icono = Icons.Outlined.Search,
        iconoSeleccionado = Icons.Filled.Search
    ),

    Destino2(
        ruta = "destino2",
        nombre = R.string.destino2,
        icono = Icons.Outlined.LocationOn,
        iconoSeleccionado = Icons.Filled.LocationOn
    ),

    Destino3(
    ruta = "destino3",
    nombre = R.string.destino3,
    icono = Icons.Outlined.Home,
    iconoSeleccionado = Icons.Filled.Home
    ),

    Destino4(
        ruta = "destino4",
        nombre = R.string.destino4,
        icono = Icons.Outlined.Face,
        iconoSeleccionado = Icons.Filled.Face
    ),

    Destino5(
        ruta = "destino5",
        nombre = R.string.destino5,
        icono = Icons.Outlined.Person,
        iconoSeleccionado = Icons.Filled.Person
    )
}