package com.example.urfungi.Recetas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urfungi.Curiosidades.Seta
import com.example.urfungi.R
import kotlin.math.min

val recipes = listOf(
    Recipe(
        "SETAS A LA PLANCHA",
        R.drawable.teemo,
        "Preparación:",
        "1. Lavar las setas y cortarlas en trozos."
    ),
    Recipe(
        "SETAS EN SALSA",
        R.drawable.teemo,
        "Preparación:",
        "1. Sofreir la cebolla y el ajo picados."
    ),
    Recipe(
        "SETAS AL AJILLO",
        R.drawable.teemo,
        "Preparación:",
        "1. Sofreir las setas y los ajos picados."
    ),
)

@Composable
fun RecetasSetasListScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredRecetasSetas = filterSetasRecetas(recipes, searchQuery.text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recetas de las Setas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        SetasSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

        RecetasSetasListContent(recipes = filteredRecetasSetas)
    }
}

@Composable
fun SetasSearchBar(searchQuery: TextFieldValue, onSearchQueryChange: (TextFieldValue) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(0.dp, 0.dp, 15.dp)
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChange(it) },
            placeholder = { Text(text = "Buscar recetas setas") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun RecetasSetasListContent(recipes: List<Recipe>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(recipes.size) { index ->
            RecetasSetasListItem(recipes = recipes[index])
        }
    }
}

@Composable
fun RecetasSetasListItem(recipes: Recipe) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 30.dp)
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = recipes.imagenResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .aspectRatio(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        text = recipes.nombre,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    if (!isExpanded) {
                        Text(
                            text = recipes.preparacion.substring(
                                0,
                                min(14, recipes.preparacion.length)
                            )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = recipes.preparacion,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nombre cientifico",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = recipes.ingredientes, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Estación", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = recipes.ingredientes, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

fun filterSetasRecetas(recipes: List<Recipe>, query: String): List<Recipe> {
    return recipes.filter {
        it.nombre.contains(query, ignoreCase = true) ||
                it.nombre.contains(query, ignoreCase = true) ||
                it.ingredientes.contains(query, ignoreCase = true) ||
                it.preparacion.contains(query, ignoreCase = true)
    }
}
