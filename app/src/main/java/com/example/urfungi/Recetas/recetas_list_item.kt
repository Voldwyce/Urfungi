package com.example.urfungi.Recetas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val recipes = listOf(
    Recipe("Setas a la plancha", "Preparación:", "1. Lavar las setas y cortarlas en trozos."),
    Recipe("Setas en salsa", "Preparación:", "1. Sofreir la cebolla y el ajo picados."),
    Recipe("Setas al ajillo", "Preparación:", "1. Sofreir las setas y los ajos picados."),
)


/* @Composable
 fun RecipeList() {
     val recipes = listOf(
         Recipe(
             "Setas a la plancha",
             "Preparación:",
             "1. Lavar las setas y cortarlas en trozos."
         ),
         Recipe("Setas en salsa", "Preparación:", "1. Sofreir la cebolla y el ajo picados."),
         Recipe("Setas al ajillo", "Preparación:", "1. Sofreir las setas y los ajos picados."),
     )
 }*/


@Composable
fun RecipeListItem(recipe: Recipe) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                imageVector = Icons.Default.Face,
                contentDescription = "Imagen de la receta",
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = recipe.nombre,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recipe.preparacion,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
                Text(
                    text = recipe.ingredientes,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            }
        }
    }
}